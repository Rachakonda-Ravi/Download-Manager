package manager.ui;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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

        // =========================
        // HEADER
        // =========================
        Label title = new Label("®️ Download Manager");
        title.setStyle("-fx-font-size:18px; -fx-font-weight:bold;");

        ComboBox<String> viewSelector = new ComboBox<>();
        viewSelector.getItems().addAll("List View", "Card View", "Table View");
        viewSelector.setValue("List View");

        Button themeBtn = new Button("Switch Theme");

        HBox header = new HBox(20, title, viewSelector, themeBtn);
        header.setPadding(new Insets(10));
        root.setTop(header);

        // =========================
        // CENTER CONTAINER
        // =========================
        contentContainer = new StackPane();
        contentContainer.setPadding(new Insets(15));
        root.setCenter(contentContainer);

        createListView();
        createCardView();
        createTableView();

        contentContainer.getChildren().add(listView); // default

        // View switching
        viewSelector.setOnAction(e -> {
            String selected = viewSelector.getValue();
            switchView(selected);
        });

        // =========================
        // BOTTOM INPUT BAR
        // =========================
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

                queue.add(task);
                downloads.add(task);

                urlField.clear();
            }
        });

        HBox bottomBar = new HBox(10, urlField, downloadBtn);
        bottomBar.setPadding(new Insets(10));
        root.setBottom(bottomBar);

        // =========================
        // SCENE
        // =========================
        scene = new Scene(root, 1000, 650);
        scene.getStylesheets().add(
                getClass().getResource("/style.css").toExternalForm()
        );

        // Simple theme switch placeholder
        themeBtn.setOnAction(e -> toggleTheme());

        stage.setTitle("®️ Download Manager");
        stage.setScene(scene);
        stage.show();
    }

    // =====================================================
    // CREATE LIST VIEW
    // =====================================================
    private void createListView() {

        listView = new ListView<>(downloads);
        listView.setPlaceholder(new Label("No downloads yet"));

        listView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(DownloadTask task, boolean empty) {
                super.updateItem(task, empty);

                if (empty || task == null) {
                    setText(null);
                } else {
                    setText(task.toString());
                }
            }
        });
    }

    // =====================================================
    // CREATE CARD VIEW (VBox)
    // =====================================================
    private void createCardView() {

        cardView = new VBox(10);
        cardView.setPadding(new Insets(10));

        downloads.addListener((javafx.collections.ListChangeListener<DownloadTask>) change -> {
            refreshCardView();
        });
    }

    private void refreshCardView() {
        cardView.getChildren().clear();

        for (DownloadTask task : downloads) {

            VBox card = new VBox(5);
            card.setPadding(new Insets(10));
            card.setStyle("-fx-border-color: gray; -fx-border-radius:5;");

            Label name = new Label(task.toString());
            ProgressBar progress = new ProgressBar(0.3);

            card.getChildren().addAll(name, progress);
            cardView.getChildren().add(card);
        }
    }

    // =====================================================
    // CREATE TABLE VIEW
    // =====================================================
    private void createTableView() {

        tableView = new TableView<>(downloads);

        TableColumn<DownloadTask, String> nameCol =
                new TableColumn<>("Download");

        nameCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().toString()
                ));

        nameCol.setPrefWidth(400);

        tableView.getColumns().add(nameCol);
    }

    // =====================================================
    // VIEW SWITCHING
    // =====================================================
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

    // =====================================================
    // THEME TOGGLE (Basic Placeholder)
    // =====================================================
    private void toggleTheme() {

        if (scene.getStylesheets().get(0).contains("style.css")) {
            scene.getStylesheets().clear();
        } else {
            scene.getStylesheets().add(
                    getClass().getResource("/style.css").toExternalForm()
            );
        }
    }
}
