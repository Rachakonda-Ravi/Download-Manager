package manager.ui;

import javafx.application.Application;
import javafx.collections.*;
import javafx.geometry.*;
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

    private TableView<DownloadTask> tableView;
    private Scene scene;

    @Override
    public void start(Stage stage) {

        BorderPane root = new BorderPane();

        // ===== LEFT SIDEBAR =====
        VBox sidebar = new VBox(20);
        sidebar.setPadding(new Insets(20));
        sidebar.setPrefWidth(220);
        sidebar.getStyleClass().add("sidebar");

        Label logo = new Label("®️ Download Manager");
        logo.getStyleClass().add("logo");

        Button dashboardBtn = new Button("Dashboard");
        Button activeBtn = new Button("Active");
        Button completedBtn = new Button("Completed");

        ComboBox<String> themeBox = new ComboBox<>();
        themeBox.getItems().addAll("Dark", "Light", "Purple-Gold");
        themeBox.setValue("Dark");

        themeBox.setOnAction(e -> {
            switch (themeBox.getValue()) {
                case "Dark":
                    ThemeManager.applyTheme(scene, ThemeManager.Theme.DARK);
                    break;
                case "Light":
                    ThemeManager.applyTheme(scene, ThemeManager.Theme.LIGHT);
                    break;
                case "Purple-Gold":
                    ThemeManager.applyTheme(scene, ThemeManager.Theme.PURPLE_GOLD);
                    break;
            }
        });

        sidebar.getChildren().addAll(
                logo,
                new Separator(),
                dashboardBtn,
                activeBtn,
                completedBtn,
                new Separator(),
                new Label("Theme"),
                themeBox
        );

        root.setLeft(sidebar);

        // ===== CENTER DASHBOARD =====
        VBox center = new VBox(15);
        center.setPadding(new Insets(20));

        HBox statsBar = new HBox(20);
        statsBar.getChildren().addAll(
                createStatCard("Total", "0"),
                createStatCard("Active", "0"),
                createStatCard("Completed", "0")
        );

        createTableView();

        center.getChildren().addAll(statsBar, tableView);
        root.setCenter(center);

        // ===== BOTTOM BAR =====
        TextField urlField = new TextField();
        urlField.setPromptText("Enter download URL");

        Button downloadBtn = new Button("Start Download");

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

        HBox bottom = new HBox(10, urlField, downloadBtn);
        bottom.setPadding(new Insets(15));
        bottom.setAlignment(Pos.CENTER_LEFT);

        root.setBottom(bottom);

        scene = new Scene(root, 1200, 750);

        ThemeManager.applyTheme(scene, ThemeManager.Theme.DARK);

        stage.setTitle("®️ Download Manager");
        stage.setScene(scene);
        stage.show();
    }

    private VBox createStatCard(String title, String value) {

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("stat-title");

        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("stat-value");

        VBox card = new VBox(5, titleLabel, valueLabel);
        card.setPadding(new Insets(15));
        card.getStyleClass().add("stat-card");

        return card;
    }

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
                new TableColumn<>("Speed");

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
}
