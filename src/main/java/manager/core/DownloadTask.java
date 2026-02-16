package manager.core;

import javafx.application.Platform;
import javafx.beans.property.*;
import manager.persistence.ResumeStore;
import manager.history.*;
import manager.security.ChecksumUtil;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadTask {

    private final String url;
    private final String outputPath;
    private String expectedHash;

    private volatile boolean paused = false;
    private volatile boolean cancelled = false;

    private final Object pauseLock = new Object();

    // ================= Properties =================
    private final LongProperty totalBytes = new SimpleLongProperty(0);
    private final LongProperty downloadedBytes = new SimpleLongProperty(0);
    private final DoubleProperty progress = new SimpleDoubleProperty(0);
    private final StringProperty status = new SimpleStringProperty("Queued");

    private final DoubleProperty speedMBps = new SimpleDoubleProperty(0);
    private final StringProperty eta = new SimpleStringProperty("∞");

    public DownloadTask(String url, String outputPath) {
        this.url = url;
        this.outputPath = outputPath;
    }

    public void setExpectedHash(String hash) {
        this.expectedHash = hash;
    }

    public void pause() {
        paused = true;
        status.set("Paused");
    }

    public void resume() {
        paused = false;
        synchronized (pauseLock) {
            pauseLock.notifyAll();
        }
        status.set("Downloading");
    }

    public void cancel() {
        cancelled = true;
        status.set("Cancelled");
    }

    public void start() {

        new Thread(() -> {

            try {
                status.set("Connecting");

                File file = new File(outputPath);
                long downloaded = ResumeStore.getProgress(url);

                HttpURLConnection conn =
                        (HttpURLConnection) new URL(url).openConnection();

                conn.setRequestProperty("Range", "bytes=" + downloaded + "-");

                long total = conn.getContentLengthLong();
                long fullSize = total + downloaded;

                Platform.runLater(() -> totalBytes.set(fullSize));

                try (InputStream in = conn.getInputStream();
                     RandomAccessFile raf = new RandomAccessFile(file, "rw")) {

                    raf.seek(downloaded);
                    byte[] buffer = new byte[8192];
                    int len;

                    long lastTime = System.nanoTime();
                    long lastBytes = downloaded;

                    status.set("Downloading");

                    while ((len = in.read(buffer)) != -1) {

                        if (cancelled) {
                            file.delete();
                            ResumeStore.clearProgress(url);
                            return;
                        }

                        if (paused) {
                            synchronized (pauseLock) {
                                pauseLock.wait();
                            }
                        }

                        raf.write(buffer, 0, len);
                        downloaded += len;

                        ResumeStore.saveProgress(url, downloaded);

                        long currentTime = System.nanoTime();
                        long timeDiff = currentTime - lastTime;

                        if (timeDiff >= 1_000_000_000) { // 1 second

                            long bytesDiff = downloaded - lastBytes;

                            double speed =
                                    (bytesDiff / 1024.0 / 1024.0) /
                                    (timeDiff / 1_000_000_000.0);

                            long remaining = fullSize - downloaded;

                            double secondsLeft =
                                    speed > 0 ?
                                            (remaining / 1024.0 / 1024.0) / speed
                                            : Double.POSITIVE_INFINITY;

                            long mins = (long) (secondsLeft / 60);
                            long secs = (long) (secondsLeft % 60);

                            String etaText =
                                    speed > 0
                                            ? mins + "m " + secs + "s"
                                            : "∞";

                            long finalDownloaded = downloaded;
                            double finalSpeed = speed;

                            Platform.runLater(() -> {
                                downloadedBytes.set(finalDownloaded);
                                progress.set((double) finalDownloaded / fullSize);
                                speedMBps.set(finalSpeed);
                                eta.set(etaText);
                            });

                            lastTime = currentTime;
                            lastBytes = downloaded;
                        }
                    }
                }

                ResumeStore.clearProgress(url);

                if (expectedHash != null) {
                    status.set("Verifying");
                    String actual = ChecksumUtil.sha256(file);
                    if (!actual.equalsIgnoreCase(expectedHash)) {
                        file.delete();
                        status.set("Checksum Failed");
                        return;
                    }
                }

                status.set("Completed");
                speedMBps.set(0);
                eta.set("Done");

                DownloadHistory.add(
                        new HistoryRecord(
                                url,
                                file.getName(),
                                outputPath,
                                file.length()
                        )
                );

            } catch (Exception e) {
                status.set("Error");
                e.printStackTrace();
            }

        }).start();
    }

    // ================= Getters =================

    public DoubleProperty progressProperty() { return progress; }
    public StringProperty statusProperty() { return status; }
    public DoubleProperty speedProperty() { return speedMBps; }
    public StringProperty etaProperty() { return eta; }
    public String getFileName() { return new File(outputPath).getName(); }
}
