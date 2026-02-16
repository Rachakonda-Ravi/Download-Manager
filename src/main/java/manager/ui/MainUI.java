package manager.ui;

import javafx.application.Application;
import javafx.collections.*;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import manager.core.DownloadTask;
import manager.queue.DownloadQueue;

public class MainUI extends Application {

    private final DownloadQueue queue = new DownloadQueue();
    private final ObservableList<DownloadTask> downloads =
            FXCollections.observableArrayList();

    private StackPane contentContainer;
    private ListView<DownloadTask> listView;
    private VBox cardView;
    private TableView<DownloadTask> tableView;
    private Scene scene;

    @Override
    public void start(Stage stage) {

        BorderPane root = new BorderPane();

        // HEADER
        Label title = new Label("®️ Download Manager");
        title.setStyle("-fx-font-size:18px; -fx-font-weight:bold;");

        ComboBox<String> viewSelector = new ComboBox<>();
        viewSelector.getItems().addAll("List View", "Card View", "Table View");
        viewSelector.setValue("List View");

        HBox header = new HBox(20, title, viewSelector);
        header.setPadding(new Insets(10));
        root.setTop(header);

        // CENTER
        contentContainer = new StackPane();
        contentContainer.setPadding(new Insets(15));
        root.setCenter(contentContainer);

        createListView();
        createCardView();
        createTableView();

        contentContainer.getChildren().add(listView);

        viewSelector.setOnAction(e -> switchView(viewSelector.getValue()));

        // BOTTOM
        TextField urlField = new TextField();
        urlField.setPromptText("Enter download URL");

        Button downloadBtn = new Button("Download");

        downloadBtn.setOnAction(e -> {
            String url = urlField.getText();
            if (!url.isEmpty()) {

                String fileName =
                        url.substring(url.lastIndexOf("/") + 1);

                DownloadTask task =
                        new DownloadTask(url, "downloads/" + fileName);

                downloads.add(task);
                queue.add(task);
                task.start();

                urlField.clear();
            }
        });

        HBox bottomBar = new HBox(10, urlField, downloadBtn);
        bottomBar.setPadding(new Insets(10));
        root.setBottom(bottomBar);

        scene = new Scene(root, 1000, 650);
        stage.setTitle("®️ Download Manager");
        stage.setScene(scene);
        stage.show();
    }

    // ================= LIST VIEW =================

    private void createListView() {

        listView = new ListView<>(downloads);

        listView.setCellFactory(param -> new ListCell<>() {

            @Override
            protected void updateItem(DownloadTask task, boolean empty) {
                super.updateItem(task, empty);

                if (empty || task == null) {
                    setGraphic(null);
                    return;
                }

                Label name = new Label(task.getFileName());

                ProgressBar bar = new ProgressBar();
                bar.setPrefWidth(450);
                bar.progressProperty().bind(task.progressProperty());

                Label percent = new Label();
                percent.textProperty().bind(
                        task.progressProperty()
                                .multiply(100)
                                .asString("%.0f%%")
                );

                Label speed = new Label();
                speed.textProperty().bind(
                        task.speedProperty()
                                .asString("Speed: %.2f MB/s")
                );

                Label eta = new Label();
                eta.textProperty().bind(
                        task.etaProperty()
                                .concat(" remaining")
                );

                Label status = new Label();
                status.textProperty().bind(task.statusProperty());

                Button pauseResume = new Button("Pause");

                pauseResume.setOnAction(e -> {
                    if (pauseResume.getText().equals("Pause")) {
                        task.pause();
                        pauseResume.setText("Resume");
                    } else {
                        task.resume();
                        pauseResume.setText("Pause");
                    }
                });

                VBox box = new VBox(
                        5,
                        name,
                        bar,
                        percent,
                        speed,
                        eta,
                        status,
                        pauseResume
                );

                box.setPadding(new Insets(8));
                box.setStyle("-fx-border-color: #444; -fx-border-radius: 5;");

                setGraphic(box);
            }
        });
    }

    // ================= CARD VIEW =================

    private void createCardView() {

        cardView = new VBox(10);

        downloads.addListener((ListChangeListener<DownloadTask>) change -> {
            refreshCardView();
        });
    }

    private void refreshCardView() {

        cardView.getChildren().clear();

        for (DownloadTask task : downloads) {

            Label name = new Label(task.getFileName());

            ProgressBar bar = new ProgressBar();
            bar.setPrefWidth(400);
            bar.progressProperty().bind(task.progressProperty());

            Label speed = new Label();
            speed.textProperty().bind(
                    task.speedProperty()
                            .asString("Speed: %.2f MB/s")
            );

            Label eta = new Label();
            eta.textProperty().bind(task.etaProperty());

            Label status = new Label();
            status.textProperty().bind(task.statusProperty());

            Button pauseResume = new Button("Pause");

            pauseResume.setOnAction(e -> {
                if (pauseResume.getText().equals("Pause")) {
                    task.pause();
                    pauseResume.setText("Resume");
                } else {
                    task.resume();
                    pauseResume.setText("Pause");
                }
            });

            VBox card = new VBox(
                    5,
                    name,
                    bar,
                    speed,
                    eta,
                    status,
                    pauseResume
            );

            card.setPadding(new Insets(10));
            card.setStyle("-fx-border-color: gray;");

            cardView.getChildren().add(card);
        }
    }

    // ================= TABLE VIEW =================

    private void createTableView() {

        tableView = new TableView<>(downloads);

        TableColumn<DownloadTask, String> nameCol =
                new TableColumn<>("File");

        nameCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getFileName()
                ));

        TableColumn<DownloadTask, Double> progressCol =
                new TableColumn<>("Progress");

        progressCol.setCellValueFactory(data ->
                data.getValue().progressProperty().asObject()
        );

        progressCol.setCellFactory(col -> new TableCell<>() {
            private final ProgressBar bar = new ProgressBar();

            @Override
            protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setGraphic(null);
                } else {
                    bar.setProgress(value);
                    setGraphic(bar);
                }
            }
        });

        TableColumn<DownloadTask, String> speedCol =
                new TableColumn<>("Speed (MB/s)");

        speedCol.setCellValueFactory(data ->
                data.getValue().speedProperty().asObject()
        );

        TableColumn<DownloadTask, String> etaCol =
                new TableColumn<>("ETA");

        etaCol.setCellValueFactory(data ->
                data.getValue().etaProperty()
        );

        TableColumn<DownloadTask, String> statusCol =
                new TableColumn<>("Status");

        statusCol.setCellValueFactory(data ->
                data.getValue().statusProperty()
        );

        tableView.getColumns().addAll(
                nameCol,
                progressCol,
                speedCol,
                etaCol,
                statusCol
        );
    }

    // ================= SWITCH VIEW =================

    private void switchView(String view) {

        contentContainer.getChildren().clear();

        switch (view) {
            case "List View":
                contentContainer.getChildren().add(listView);
                break;
            case "Card View":
                refreshCardView();
                contentContainer.getChildren().add(cardView);
                break;
            case "Table View":
                contentContainer.getChildren().add(tableView);
                break;
        }
    }
}
