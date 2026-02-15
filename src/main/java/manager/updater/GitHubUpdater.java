package manager.updater;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

public class GitHubUpdater {

    private static final String API =
        "https://api.github.com/repos/Rachakonda-Ravi/Download-Manager/releases/latest";

    public static String getLatestVersion() {
        try {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(new URL(API).openStream())
            );

            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("\"tag_name\"")) {
                    return line.split(":")[1].replace("\"","").replace(",","").trim();
                }
            }
        } catch (Exception ignored) {}

        return null;
    }
}
