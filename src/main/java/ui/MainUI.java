package manager.ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import manager.core.DownloadTask;
import manager.queue.DownloadQueue;

public class MainUI extends Application {

    private final DownloadQueue queue = new DownloadQueue();

    @Override
    public void start(Stage stage) {

        TextField urlField = new TextField();
        urlField.setPromptText("Enter download URL");

        Button downloadBtn = new Button("Download");

        downloadBtn.setOnAction(e -> {
            String url = urlField.getText();
            if (!url.isEmpty()) {
                String fileName = url.substring(url.lastIndexOf("/") + 1);
                DownloadTask task = new DownloadTask(url, "downloads/" + fileName);
                queue.add(task);
            }
        });

        VBox root = new VBox(10, urlField, downloadBtn);
        root.setStyle("-fx-padding:20;");

        Scene scene = new Scene(root, 400, 200);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        stage.setTitle("Download Manager");
        stage.setScene(scene);
        stage.show();
    }
}
