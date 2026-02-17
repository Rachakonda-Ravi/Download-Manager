package manager.ui;

import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class MainUI extends Application {

    private Scene scene;

    @Override
    public void start(Stage stage) {

        BorderPane root = new BorderPane();
        root.getStyleClass().add("root");

        // ===== SIDEBAR =====
        VBox sidebar = new VBox(25);
        sidebar.setPadding(new Insets(25));
        sidebar.setPrefWidth(250);
        sidebar.getStyleClass().add("sidebar");

        Label logo = new Label("Download Manager");
        logo.getStyleClass().add("logo");

        ComboBox<String> themeSelector = new ComboBox<>();
        themeSelector.getItems().addAll(
                "Dark (Neon Hacker)",
                "Light (Glass)",
                "Aurora (Gaming)"
        );
        themeSelector.setValue("Dark (Neon Hacker)");

        themeSelector.setOnAction(e -> switchTheme(themeSelector.getValue()));

        VBox nav = new VBox(12,
                createNavButton("Dashboard"),
                createNavButton("Active"),
                createNavButton("Completed"),
                createNavButton("Settings")
        );

        sidebar.getChildren().addAll(logo, themeSelector, nav);
        root.setLeft(sidebar);

        // ===== CENTER =====
        VBox center = new VBox(20);
        center.setPadding(new Insets(25));

        Label title = new Label("Dashboard");
        title.getStyleClass().add("title");

        VBox downloads = new VBox(15,
                createDownloadCard("ubuntu.iso", 0.6),
                createDownloadCard("video.mp4", 1.0),
                createDownloadCard("backup.zip", 0.1)
        );

        center.getChildren().addAll(title, downloads);
        root.setCenter(center);

        // ===== BOTTOM BAR =====
        HBox bottom = new HBox(15);
        bottom.setPadding(new Insets(15));
        bottom.setAlignment(Pos.CENTER);

        TextField urlField = new TextField();
        urlField.setPromptText("Paste download URL here...");
        urlField.getStyleClass().add("url-input");
        HBox.setHgrow(urlField, Priority.ALWAYS);

        Button addBtn = new Button("Add");
        addBtn.getStyleClass().add("primary-button");

        bottom.getChildren().addAll(urlField, addBtn);
        root.setBottom(bottom);

        scene = new Scene(root, 1300, 800);
        scene.getStylesheets().add(
                getClass().getResource("/dark.css").toExternalForm()
        );

        stage.setTitle("Download Manager");
        stage.setScene(scene);
        stage.show();
    }

    private void switchTheme(String theme) {

        scene.getStylesheets().clear();

        String css = "/dark.css";

        if (theme.contains("Light"))
            css = "/light.css";
        else if (theme.contains("Aurora"))
            css = "/aurora.css";

        scene.getStylesheets().add(
                getClass().getResource(css).toExternalForm()
        );
    }

    private Button createNavButton(String text) {
        Button btn = new Button(text);
        btn.getStyleClass().add("nav-button");
        btn.setMaxWidth(Double.MAX_VALUE);
        return btn;
    }

    private VBox createDownloadCard(String file, double progress) {

        Label name = new Label(file);
        name.getStyleClass().add("file-name");

        ProgressBar bar = new ProgressBar(progress);
        bar.getStyleClass().add("modern-progress");

        VBox box = new VBox(10, name, bar);
        box.setPadding(new Insets(18));
        box.getStyleClass().add("download-card");

        return box;
    }
}
