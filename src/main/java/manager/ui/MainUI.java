package manager.ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import manager.core.DownloadTask;

import java.io.File;
import java.util.*;

public class MainUI extends Application {

    private VBox downloadsBox;
    private Scene scene;

    @Override
    public void start(Stage stage) {

        BorderPane root = new BorderPane();
        root.getStyleClass().add("root");

        // ===== SIDEBAR =====
        VBox sidebar = new VBox(20);
        sidebar.setPadding(new Insets(20));
        sidebar.setPrefWidth(240);
        sidebar.getStyleClass().add("sidebar");

        Label logo = new Label("Download Manager");
        logo.getStyleClass().add("logo");

        ComboBox<String> themeSelector = new ComboBox<>();
        themeSelector.getItems().addAll(
                "Dark",
                "Light",
                "Aurora"
        );
        themeSelector.setValue("Dark");
        themeSelector.setOnAction(e -> switchTheme(themeSelector.getValue()));

        sidebar.getChildren().addAll(logo, themeSelector);
        root.setLeft(sidebar);

        // ===== CENTER =====
        downloadsBox = new VBox(15);
        downloadsBox.setPadding(new Insets(20));

        ScrollPane scroll = new ScrollPane(downloadsBox);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent;");
        root.setCenter(scroll);

        // ===== BOTTOM =====
        HBox bottom = new HBox(10);
        bottom.setPadding(new Insets(15));

        TextField urlField = new TextField();
        urlField.setPromptText("Paste URL...");
        urlField.getStyleClass().add("url-input");
        HBox.setHgrow(urlField, Priority.ALWAYS);

        Button addBtn = new Button("Download");
        addBtn.getStyleClass().add("primary-button");

        addBtn.setOnAction(e -> {
            String url = urlField.getText();
            if (url.isEmpty()) return;

            String fileName = url.substring(url.lastIndexOf("/") + 1);
            String output = new File("downloads", fileName).getAbsolutePath();

            DownloadTask task = new DownloadTask(url, output);
            addDownloadCard(task, fileName);
            task.start();
        });

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

        if (theme.equals("Light")) css = "/light.css";
        if (theme.equals("Aurora")) css = "/aurora.css";

        scene.getStylesheets().add(
                getClass().getResource(css).toExternalForm()
        );
    }

    private void addDownloadCard(DownloadTask task, String fileName) {

        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.getStyleClass().add("download-card");

        Label name = new Label(fileName);
        name.getStyleClass().add("file-name");

        ProgressBar bar = new ProgressBar();
        bar.progressProperty().bind(task.progressProperty());

        Label status = new Label();
        status.textProperty().bind(task.statusProperty());

        Label speed = new Label();
        speed.textProperty().bind(
                Bindings.format("Speed: %.2f MB/s", task.speedProperty())
        );

        HBox controls = new HBox(10);

        Button pause = new Button("Pause");
        pause.setOnAction(e -> task.pause());

        Button resume = new Button("Resume");
        resume.setOnAction(e -> task.resume());

        Button cancel = new Button("Cancel");
        cancel.setOnAction(e -> {
            task.cancel();
            downloadsBox.getChildren().remove(card);
        });

        controls.getChildren().addAll(pause, resume, cancel);

        card.getChildren().addAll(name, bar, status, speed, controls);

        downloadsBox.getChildren().add(card);
    }

    public static void main(String[] args) {
        launch();
    }
}
