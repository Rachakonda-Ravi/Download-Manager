package manager.persistence;

import java.io.*;
import java.util.Properties;

public class ResumeStore {

    private static final String RESUME_FILE = "downloads/resume.properties";

    public static long getProgress(String url) {
        try {
            Properties prop = new Properties();
            File file = new File(RESUME_FILE);
            if (!file.exists()) return 0;

            prop.load(new FileInputStream(file));
            return Long.parseLong(prop.getProperty(url, "0"));

        } catch (Exception e) {
            return 0;
        }
    }

    public static void saveProgress(String url, long bytes) {
        try {
            new File("downloads").mkdirs();

            Properties prop = new Properties();
            File file = new File(RESUME_FILE);

            if (file.exists())
                prop.load(new FileInputStream(file));

            prop.setProperty(url, String.valueOf(bytes));
            prop.store(new FileOutputStream(file), null);

        } catch (Exception ignored) {}
    }

    public static void clearProgress(String url) {
        try {
            Properties prop = new Properties();
            File file = new File(RESUME_FILE);
            if (!file.exists()) return;

            prop.load(new FileInputStream(file));
            prop.remove(url);
            prop.store(new FileOutputStream(file), null);

        } catch (Exception ignored) {}
    }
}
