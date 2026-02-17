package manager.core;

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

    private final DoubleProperty progress = new SimpleDoubleProperty(0);
    private final DoubleProperty speed = new SimpleDoubleProperty(0);
    private final StringProperty status = new SimpleStringProperty("Queued");

    private volatile boolean paused = false;
    private volatile boolean cancelled = false;

    public DownloadTask(String url, String outputPath) {
        this.url = url;
        this.outputPath = outputPath;
    }

    public DoubleProperty progressProperty() { return progress; }
    public DoubleProperty speedProperty() { return speed; }
    public StringProperty statusProperty() { return status; }

    public void pause() { paused = true; status.set("Paused"); }
    public void resume() { paused = false; status.set("Downloading"); }
    public void cancel() { cancelled = true; status.set("Cancelled"); }

    public void start() {

        new Thread(() -> {
            try {
                File file = new File(outputPath);
                long downloaded = ResumeStore.getProgress(url);

                HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setRequestProperty("Range", "bytes=" + downloaded + "-");

                long totalSize = conn.getContentLengthLong() + downloaded;

                try (InputStream in = conn.getInputStream();
                     RandomAccessFile raf = new RandomAccessFile(file, "rw")) {

                    raf.seek(downloaded);
                    byte[] buffer = new byte[8192];
                    int len;

                    long lastTime = System.currentTimeMillis();
                    long lastBytes = downloaded;

                    status.set("Downloading");

                    while ((len = in.read(buffer)) != -1) {

                        if (cancelled) {
                            file.delete();
                            ResumeStore.clearProgress(url);
                            return;
                        }

                        while (paused) Thread.sleep(200);

                        raf.write(buffer, 0, len);
                        downloaded += len;

                        progress.set((double) downloaded / totalSize);

                        long now = System.currentTimeMillis();
                        if (now - lastTime >= 1000) {
                            double mbps = ((downloaded - lastBytes) / 1024.0 / 1024.0);
                            speed.set(mbps);
                            lastBytes = downloaded;
                            lastTime = now;
                        }

                        ResumeStore.saveProgress(url, downloaded);
                    }
                }

                status.set("Completed");
                ResumeStore.clearProgress(url);

            } catch (Exception e) {
                status.set("Error");
                e.printStackTrace();
            }
        }).start();
    }
}
