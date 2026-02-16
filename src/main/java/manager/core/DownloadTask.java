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

    // ==============================
    // JavaFX Properties
    // ==============================
    private final LongProperty totalBytes = new SimpleLongProperty(0);
    private final LongProperty downloadedBytes = new SimpleLongProperty(0);
    private final DoubleProperty progress = new SimpleDoubleProperty(0);
    private final StringProperty status = new SimpleStringProperty("Queued");

    public DownloadTask(String url, String outputPath) {
        this.url = url;
        this.outputPath = outputPath;
    }

    public void setExpectedHash(String hash) {
        this.expectedHash = hash;
    }

    public void pause() { paused = true; status.set("Paused"); }
    public void cancel() { cancelled = true; status.set("Cancelled"); }

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

                    status.set("Downloading");

                    while ((len = in.read(buffer)) != -1) {

                        if (cancelled) {
                            file.delete();
                            ResumeStore.clearProgress(url);
                            return;
                        }

                        while (paused) {
                            Thread.sleep(200);
                        }

                        raf.write(buffer, 0, len);
                        downloaded += len;

                        long finalDownloaded = downloaded;

                        ResumeStore.saveProgress(url, downloaded);

                        Platform.runLater(() -> {
                            downloadedBytes.set(finalDownloaded);
                            if (fullSize > 0) {
                                progress.set((double) finalDownloaded / fullSize);
                            }
                        });
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

    // ==============================
    // Property Getters
    // ==============================
    public DoubleProperty progressProperty() {
        return progress;
    }

    public StringProperty statusProperty() {
        return status;
    }

    public LongProperty downloadedBytesProperty() {
        return downloadedBytes;
    }

    public LongProperty totalBytesProperty() {
        return totalBytes;
    }

    public String getFileName() {
        return new File(outputPath).getName();
    }

    @Override
    public String toString() {
        return getFileName();
    }
}
