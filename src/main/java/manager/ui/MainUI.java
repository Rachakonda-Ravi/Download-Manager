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
        root.getStyleClass().add("root-bg");

        // ===== LEFT SIDEBAR =====
        VBox sidebar = new VBox(25);
        sidebar.setPadding(new Insets(30));
        sidebar.setPrefWidth(260);
        sidebar.getStyleClass().add("sidebar");

        Label logo = new Label("Download Manager");
        logo.getStyleClass().add("logo");

        VBox nav = new VBox(15,
                createNavButton("Dashboard"),
                createNavButton("Active Downloads"),
                createNavButton("Completed"),
                createNavButton("Settings"),
                createNavButton("About")
        );

        sidebar.getChildren().addAll(logo, nav);
        root.setLeft(sidebar);

        // ===== CENTER PANEL =====
        VBox centerContainer = new VBox(25);
        centerContainer.setPadding(new Insets(30));

        Label dashTitle = new Label("Dashboard");
        dashTitle.getStyleClass().add("title");

        HBox mainContent = new HBox(25);

        VBox downloadsPanel = new VBox(20);
        downloadsPanel.getStyleClass().add("glass-panel");
        downloadsPanel.setPadding(new Insets(25));
        downloadsPanel.setPrefWidth(650);

        Label activeTitle = new Label("Active & recent downloads");
        activeTitle.getStyleClass().add("section-title");

        downloadsPanel.getChildren().addAll(
                activeTitle,
                createDownloadCard("ubuntu.iso", "Downloading", 0.6),
                createDownloadCard("video_tutorial.mp4", "Completed", 1.0),
                createDownloadCard("archive_backup.zip", "Queued", 0.1)
        );

        VBox overviewPanel = new VBox(20);
        overviewPanel.getStyleClass().add("glass-panel");
        overviewPanel.setPadding(new Insets(25));
        overviewPanel.setPrefWidth(320);

        overviewPanel.getChildren().addAll(
                createOverviewItem("Active", "1 download"),
                createOverviewItem("Queued", "2 items"),
                createOverviewItem("Completed today", "5 files"),
                createOverviewItem("Total speed", "18.4 MB/s"),
                createOverviewItem("Disk", "74% free")
        );

        mainContent.getChildren().addAll(downloadsPanel, overviewPanel);

        centerContainer.getChildren().addAll(dashTitle, mainContent);
        root.setCenter(centerContainer);

        // ===== BOTTOM BAR =====
        HBox bottomBar = new HBox(15);
        bottomBar.setPadding(new Insets(20));
        bottomBar.setAlignment(Pos.CENTER);

        TextField urlField = new TextField();
        urlField.setPromptText("Paste download URL here...");
        urlField.setPrefWidth(800);
        urlField.getStyleClass().add("url-input");

        Button addBtn = new Button("Add");
        addBtn.getStyleClass().add("primary-button");

        bottomBar.getChildren().addAll(urlField, addBtn);
        root.setBottom(bottomBar);

        scene = new Scene(root, 1400, 820);
        scene.getStylesheets().add(
                getClass().getResource("/dashboard.css").toExternalForm()
        );

        stage.setTitle("Download Manager");
        stage.setScene(scene);
        stage.show();
    }

    private Button createNavButton(String text) {
        Button btn = new Button(text);
        btn.getStyleClass().add("nav-button");
        btn.setMaxWidth(Double.MAX_VALUE);
        return btn;
    }

    private VBox createDownloadCard(String file, String status, double progress) {

        Label fileName = new Label(file);
        fileName.getStyleClass().add("file-name");

        ProgressBar bar = new ProgressBar(progress);
        bar.getStyleClass().add("modern-bar");

        Label statusLabel = new Label(status);
        statusLabel.getStyleClass().add("badge");

        VBox box = new VBox(10, fileName, bar, statusLabel);
        box.getStyleClass().add("download-card");
        box.setPadding(new Insets(20));

        return box;
    }

    private VBox createOverviewItem(String title, String value) {
        Label t = new Label(title);
        t.getStyleClass().add("overview-title");

        Label v = new Label(value);
        v.getStyleClass().add("overview-value");

        VBox box = new VBox(5, t, v);
        box.getStyleClass().add("overview-item");
        box.setPadding(new Insets(15));
        return box;
    }
}
