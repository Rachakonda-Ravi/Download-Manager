package manager.ui;

import javafx.scene.Scene;

public class ThemeManager {

    public enum Theme {
        DARK,
        LIGHT,
        PURPLE_GOLD
    }

    private static Theme currentTheme = Theme.DARK;

    public static void applyTheme(Scene scene, Theme theme) {
        currentTheme = theme;
        scene.getStylesheets().clear();

        switch (theme) {
            case DARK:
                scene.getStylesheets().add(
                        ThemeManager.class.getResource("/dashboard.css").toExternalForm()
                );
                break;

            case LIGHT:
                scene.getStylesheets().add(
                        ThemeManager.class.getResource("/dashboard.css").toExternalForm()
                );
                break;

            case PURPLE_GOLD:
                scene.getStylesheets().add(
                        ThemeManager.class.getResource("/dashboard.css").toExternalForm()
                );
                break;
        }
    }

    public static Theme getCurrentTheme() {
        return currentTheme;
    }
}
