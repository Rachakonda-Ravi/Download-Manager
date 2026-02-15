package manager.history;

import java.time.LocalDateTime;

public class HistoryRecord {

    public String url;
    public String fileName;
    public String path;
    public long size;
    public String date;

    public HistoryRecord(String url, String fileName, String path, long size) {
        this.url = url;
        this.fileName = fileName;
        this.path = path;
        this.size = size;
        this.date = LocalDateTime.now().toString();
    }
}
