package manager.history;

import com.google.gson.*;
import java.io.*;
import java.util.*;

public class DownloadHistory {

    private static final String FILE = "downloads/history.json";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static void add(HistoryRecord record) {
        List<HistoryRecord> records = load();
        records.add(record);
        save(records);
    }

    public static List<HistoryRecord> load() {
        try {
            File file = new File(FILE);
            if (!file.exists()) return new ArrayList<>();

            return Arrays.asList(
                gson.fromJson(new FileReader(file), HistoryRecord[].class)
            );
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private static void save(List<HistoryRecord> records) {
        try {
            new File("downloads").mkdirs();
            gson.toJson(records, new FileWriter(FILE));
        } catch (Exception ignored) {}
    }
}
