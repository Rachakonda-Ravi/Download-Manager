package manager.core;

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

    public DownloadTask(String url, String outputPath) {
        this.url = url;
        this.outputPath = outputPath;
    }

    public void setExpectedHash(String hash) {
        this.expectedHash = hash;
    }

    public void pause() { paused = true; }
    public void cancel() { cancelled = true; }

    public void start() {

        try {
            File file = new File(outputPath);
            long downloaded = ResumeStore.getProgress(url);

            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestProperty("Range", "bytes=" + downloaded + "-");

            try (InputStream in = conn.getInputStream();
                 RandomAccessFile raf = new RandomAccessFile(file, "rw")) {

                raf.seek(downloaded);
                byte[] buffer = new byte[8192];
                int len;

                while ((len = in.read(buffer)) != -1) {

                    if (cancelled) {
                        file.delete();
                        ResumeStore.clearProgress(url);
                        return;
                    }

                    while (paused) Thread.sleep(200);

                    raf.write(buffer, 0, len);
                    downloaded += len;
                    ResumeStore.saveProgress(url, downloaded);
                }
            }

            ResumeStore.clearProgress(url);

            if (expectedHash != null) {
                String actual = ChecksumUtil.sha256(file);
                if (!actual.equalsIgnoreCase(expectedHash)) {
                    file.delete();
                    return;
                }
            }

            DownloadHistory.add(
                new HistoryRecord(url, file.getName(), outputPath, file.length())
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
