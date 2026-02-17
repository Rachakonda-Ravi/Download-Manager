package manager.ui;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import manager.core.DownloadTask;

import java.io.File;

public class MainUI extends Application {

    private VBox downloadsBox;
    private Scene scene;

    @Override
    public void start(Stage stage) {

        BorderPane root = new BorderPane();
        root.getStyleClass().add("root");

        // ===== SIDEBAR =====
        VBox sidebar = new VBox(25);
        sidebar.setPadding(new Insets(30));
        sidebar.setPrefWidth(260);
        sidebar.getStyleClass().add("sidebar");

        Label logo = new Label("Download Manager");
        logo.getStyleClass().add("logo");

        ComboBox<String> themeSelector = new ComboBox<>();
        themeSelector.getItems().addAll("Light", "Dark", "Aurora");
        themeSelector.setValue("Light"); // DEFAULT MODE
        themeSelector.setOnAction(e -> switchTheme(themeSelector.getValue()));

        sidebar.getChildren().addAll(logo, themeSelector);
        root.setLeft(sidebar);

        // ===== CENTER =====
        downloadsBox = new VBox(18);
        downloadsBox.setPadding(new Insets(30));

        ScrollPane scroll = new ScrollPane(downloadsBox);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent;");
        root.setCenter(scroll);

        // ===== BOTTOM BAR =====
        HBox bottom = new HBox(15);
        bottom.setPadding(new Insets(20));
        bottom.setAlignment(Pos.CENTER);

        TextField urlField = new TextField();
        urlField.setPromptText("Paste download URL here...");
        urlField.getStyleClass().add("url-input");
        HBox.setHgrow(urlField, Priority.ALWAYS);

        Button addBtn = new Button("Add");
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

        scene = new Scene(root, 1350, 820);
        scene.getStylesheets().add(
                getClass().getResource("/light.css").toExternalForm()
        );

        stage.setTitle("Download Manager");
        stage.setScene(scene);
        stage.show();
    }

    private void switchTheme(String theme) {

        scene.getStylesheets().clear();

        String css = "/light.css";

        if (theme.equals("Dark")) css = "/dark.css";
        if (theme.equals("Aurora")) css = "/aurora.css";

        scene.getStylesheets().add(
                getClass().getResource(css).toExternalForm()
        );
    }

    private void addDownloadCard(DownloadTask task, String fileName) {

        VBox card = new VBox(12);
        card.setPadding(new Insets(20));
        card.getStyleClass().add("download-card");

        Label name = new Label(fileName);
        name.getStyleClass().add("file-name");

        ProgressBar bar = new ProgressBar();
        bar.progressProperty().bind(task.progressProperty());
        bar.getStyleClass().add("modern-progress");

        Label status = new Label();
        status.textProperty().bind(task.statusProperty());
        status.getStyleClass().add("status-label");

        Label speed = new Label();
        speed.textProperty().bind(
                Bindings.format("Speed: %.2f MB/s", task.speedProperty())
        );
        speed.getStyleClass().add("speed-label");

        HBox controls = new HBox(10);

        Button pause = new Button("Pause");
        Button resume = new Button("Resume");
        Button cancel = new Button("Cancel");

        pause.getStyleClass().add("secondary-button");
        resume.getStyleClass().add("secondary-button");
        cancel.getStyleClass().add("danger-button");

        pause.setOnAction(e -> task.pause());
        resume.setOnAction(e -> task.resume());
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
