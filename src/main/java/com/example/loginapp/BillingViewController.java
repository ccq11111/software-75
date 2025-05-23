package com.example.loginapp;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.*;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.time.LocalTime;
import java.math.BigDecimal;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;
import java.time.format.DateTimeFormatter;
import javafx.scene.control.SelectionMode;
import java.util.ArrayList;
import javafx.stage.FileChooser;
import java.io.File;
import javafx.application.Platform;
import javafx.scene.control.ButtonType;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar;
import javafx.scene.layout.GridPane;
import java.util.Optional;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.DoubleStringConverter;

import com.example.loginapp.api.BillingService;
import com.example.loginapp.api.RealBillingService;
import com.example.loginapp.api.ApiException;
import com.example.loginapp.api.BillingEntryResponse;
import com.example.loginapp.api.ApiServiceFactory;
import com.example.loginapp.api.ApiResponse;

public class BillingViewController {
    
    private static final Logger logger = Logger.getLogger(BillingViewController.class.getName());
    
    @FXML private Label usernameLabel;
    @FXML private Button addCsvButton;
    @FXML private TextField categoryField;
    @FXML private TextField productField;
    @FXML private TextField priceField;
    @FXML private TextField remarkField;
    @FXML private DatePicker timePicker;

    @FXML private Button resetButton;
    @FXML private Button buildButton;
    @FXML private Button inquireButton;
    @FXML private Button refreshButton;
    @FXML private Button deleteButton;
    @FXML private Button modifyButton;

    @FXML private TableView<BillingEntry> billingTable;
    @FXML private TableColumn<BillingEntry, String> categoryColumn;
    @FXML private TableColumn<BillingEntry, String> productColumn;
    @FXML private TableColumn<BillingEntry, String> timeColumn;
    @FXML private TableColumn<BillingEntry, Double> priceColumn;
    @FXML private TableColumn<BillingEntry, String> remarkColumn;

    private final ObservableList<BillingEntry> billingData = FXCollections.observableArrayList();
    
    private BillingService billingService;
    private String token;
    private String username;

    @FXML
    public void initialize() {
        initializeEditableColumns();
        
        billingTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        billingTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            updateDeleteButtonState();
            updateModifyButtonState();
        });

        priceColumn.setCellFactory(column -> new TableCell<BillingEntry, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    String formattedPrice = String.format("%s¥%.2f", item >= 0 ? "+" : "", item);
                    setText(formattedPrice);
                    setStyle("-fx-text-fill: " + (item >= 0 ? "#4CAF50" : "#F44336") + "; -fx-font-weight: bold;");
                }
            }
        });

        categoryColumn.setCellFactory(column -> new TableCell<BillingEntry, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item);
                }
            }
        });

        billingTable.setItems(billingData);
        timePicker.setValue(LocalDate.now());
        
        // 每次打开程序时清空CSV文件
        clearBillingEntriesFile();
        
        try {
            String factoryToken = ApiServiceFactory.getInstance().getToken();
            if (factoryToken != null && !factoryToken.isEmpty()) {
                setToken(factoryToken);
            } else {
                addSampleData();
            }
        } catch (Exception e) {
            addSampleData();
        }

        addCsvButton.setOnAction(event -> handleAddCsv());
        resetButton.setOnAction(event -> clearInputFields());
        buildButton.setOnAction(event -> handleAddRecord());
        inquireButton.setOnAction(event -> handleFuzzySearch());
        modifyButton.setOnAction(event -> handleModifyRecord());
        deleteButton.setOnAction(event -> handleDeleteRecords());

        modifyButton.setDisable(true);
        updateDeleteButtonState();
    }

    public void setToken(String token) {
        this.token = token;
        this.billingService = new RealBillingService(token);
    }
    
    private boolean needTokenRefresh() {
        return token == null || token.isEmpty();
    }

    private void addSampleData() {
        if (billingService == null) {
            billingData.add(new BillingEntry("Living costs", "Nov.utilities bill", -200.00, LocalDate.of(2024, 12, 24), "14:29"));
            billingData.add(new BillingEntry("Communication", "Dad phone bill", -200.00, LocalDate.of(2024, 12, 23), "14:29"));
            billingData.add(new BillingEntry("Salary", "Nov.salary", 18800.00, LocalDate.of(2024, 12, 21), "14:29"));
            billingData.add(new BillingEntry("Cosmetics", "-", -300.00, LocalDate.of(2024, 12, 13), "14:29"));
        }
    }

    private void handleAddRecord() {
        if (needTokenRefresh()) {
            return;
        }
        
        try {
            String category = categoryField.getText().trim();
            String product = productField.getText().trim();
            String remark = remarkField.getText().trim();
            double price = Double.parseDouble(priceField.getText().trim());
            LocalDate date = timePicker.getValue();

            if (category.isEmpty() || product.isEmpty() || date == null) {
                return;
            }

            if (remark.isEmpty()) {
                remark = product;
            }

            BillingEntry uiEntry = new BillingEntry(category, product, price, date, "14:29");
            uiEntry.remarkProperty().set(remark);
            billingData.add(uiEntry);
            
            if (billingService != null) {
                BigDecimal bdPrice = BigDecimal.valueOf(price);
                try {
                    billingService.createEntry(
                        category, product, bdPrice, date, LocalTime.now(), remark
                    );
                } catch (ApiException e) {
                    // 忽略API错误
                }
            }
            
            clearInputFields();
            saveToCsvFile();
        } catch (NumberFormatException e) {
            // 价格格式错误，忽略
        }
    }

    private void handleFuzzySearch() {
        String searchTerm = productField.getText().trim();
        if (searchTerm.isEmpty()) {
            billingData.clear();
            tryReadFromCsvFile();
            return;
        }

        ObservableList<BillingEntry> searchResults = FXCollections.observableArrayList();
        for (BillingEntry entry : billingData) {
            if (entry.productProperty().get().equalsIgnoreCase(searchTerm) ||
                entry.remarkProperty().get().toLowerCase().contains(searchTerm.toLowerCase())) {
                searchResults.add(entry);
            }
        }

        billingTable.setItems(searchResults);
        inquireButton.setText("Show All");
        inquireButton.setOnAction(e -> resetSearch());
    }
    
    private void resetSearch() {
        productField.clear();
        billingData.clear();
        tryReadFromCsvFile();
        inquireButton.setText("Inquire");
        inquireButton.setOnAction(e -> handleFuzzySearch());
    }
    
    private void tryReadFromCsvFile() {
        try {
            String csvFilePath = "data/billing/billingEntries.csv";
            File csvFile = new File(csvFilePath);
            
            if (!csvFile.exists()) {
                return;
            }
            
            List<String> lines = java.nio.file.Files.readAllLines(csvFile.toPath(), java.nio.charset.StandardCharsets.UTF_8);
            if (lines.isEmpty()) {
                return;
            }
            
            billingData.clear();
            boolean isFirstLine = true;
            for (String line : lines) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                
                try {
                    String[] parts = line.split(",");
                    String category = parts[0].trim();
                    String product = parts[1].trim();
                    
                    // 支持yyyy-MM-dd和yyyy/MM/dd两种格式
                    LocalDate date;
                    String dateStr = parts[2].trim();
                    try {
                        if (dateStr.contains("/")) {
                            // 处理yyyy/MM/dd格式
                            String[] dateParts = dateStr.split("/");
                            int year = Integer.parseInt(dateParts[0]);
                            int month = Integer.parseInt(dateParts[1]);
                            int day = Integer.parseInt(dateParts[2]);
                            date = LocalDate.of(year, month, day);
                        } else {
                            // 标准ISO格式yyyy-MM-dd
                            date = LocalDate.parse(dateStr);
                        }
                    } catch (Exception e) {
                        date = LocalDate.now();
                    }
                    
                    String time = parts[3].trim();
                    double price = Double.parseDouble(parts[4].trim());
                    String remark = parts.length > 5 ? parts[5].trim() : product;

                    BillingEntry entry = new BillingEntry(category, product, price, date, time);
                    entry.remarkProperty().set(remark);
                    billingData.add(entry);
                } catch (Exception e) {
                    logger.warning("Error reading line: " + line);
                }
            }
            
            billingTable.setItems(billingData);
        } catch (Exception e) {
            logger.severe("Failed to read CSV file: " + e.getMessage());
        }
    }

    private void clearInputFields() {
        categoryField.clear();
        productField.clear();
        priceField.clear();
        remarkField.clear();
        timePicker.setValue(LocalDate.now());
        billingTable.setItems(billingData);
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("错误");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setUsername(String username) {
        this.username = username;
        if (usernameLabel != null) {
            usernameLabel.setText(username);
        }
    }

    public String getUsername() {
        return username;
    }

    private void handleDeleteRecords() {
        ObservableList<BillingEntry> selectedEntries = billingTable.getSelectionModel().getSelectedItems();
        if (selectedEntries.isEmpty()) {
            return;
        }
        
        // 直接删除选中的记录，不显示确认对话框
        deleteSelectedEntries(selectedEntries);
    }
    
    private void deleteSelectedEntries(ObservableList<BillingEntry> selectedEntries) {
        List<BillingEntry> entriesToDelete = new ArrayList<>(selectedEntries);
        for (BillingEntry entry : entriesToDelete) {
            try {
                billingData.remove(entry);
            } catch (Exception e) {
                // 忽略删除失败
            }
        }
        
        // 同步到CSV文件
        saveToCsvFile();
        billingTable.refresh();
    }

    private void updateDeleteButtonState() {
        if (deleteButton != null) {
            boolean hasSelection = !billingTable.getSelectionModel().isEmpty();
            deleteButton.setDisable(!hasSelection);
            int selectedCount = billingTable.getSelectionModel().getSelectedItems().size();
            if (selectedCount > 0) {
                deleteButton.setTooltip(new Tooltip("Delete selected " + selectedCount + " records"));
            } else {
                deleteButton.setTooltip(new Tooltip("Select records to delete"));
            }
        }
    }

    private void updateModifyButtonState() {
        if (modifyButton != null) {
            boolean hasSingleSelection = billingTable.getSelectionModel().getSelectedItems().size() == 1;
            modifyButton.setDisable(!hasSingleSelection);
            if (hasSingleSelection) {
                modifyButton.setTooltip(new Tooltip("Modify the selected record"));
            } else {
                modifyButton.setTooltip(new Tooltip("Select one record to modify"));
            }
        }
    }

    private void initializeEditableColumns() {
        categoryColumn.setCellValueFactory(cellData -> cellData.getValue().categoryProperty());
        categoryColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        categoryColumn.setOnEditCommit(event -> {
            BillingEntry entry = event.getRowValue();
            entry.categoryProperty().set(event.getNewValue());
        });

        productColumn.setCellValueFactory(cellData -> cellData.getValue().productProperty());
        productColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        productColumn.setOnEditCommit(event -> {
            BillingEntry entry = event.getRowValue();
            entry.productProperty().set(event.getNewValue());
        });

        priceColumn.setCellValueFactory(cellData -> cellData.getValue().priceProperty().asObject());
        priceColumn.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        priceColumn.setOnEditCommit(event -> {
            BillingEntry entry = event.getRowValue();
            entry.priceProperty().set(event.getNewValue());
        });

        remarkColumn.setCellValueFactory(cellData -> cellData.getValue().remarkProperty());
        remarkColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        remarkColumn.setOnEditCommit(event -> {
            BillingEntry entry = event.getRowValue();
            entry.remarkProperty().set(event.getNewValue());
        });
        
        timeColumn.setCellValueFactory(cellData -> cellData.getValue().formattedTimeProperty());
    }

    private void clearBillingEntriesFile() {
        try {
            String csvFilePath = "data/billing/billingEntries.csv";
            File csvFile = new File(csvFilePath);
            
            csvFile.getParentFile().mkdirs();
            
            if (!csvFile.exists()) {
                csvFile.createNewFile();
            }
            
            java.io.FileWriter writer = new java.io.FileWriter(csvFile);
            writer.write("Category,Product,Date,Time,Price,Remark\n");
            writer.close();
            
            billingData.clear();
            billingTable.refresh();
        } catch (Exception e) {
            logger.severe("Failed to clear billing entries file: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddCsv() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择CSV文件");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );
        
        File selectedFile = fileChooser.showOpenDialog(billingTable.getScene().getWindow());
        if (selectedFile != null) {
            try {
                List<String> lines = java.nio.file.Files.readAllLines(selectedFile.toPath(), java.nio.charset.StandardCharsets.UTF_8);
                
                for (int i = 1; i < lines.size(); i++) {
                    try {
                        String line = lines.get(i);
                        String[] parts = line.split(",");
                        
                        // 检查CSV格式是否有足够的列
                        if (parts.length < 5) {
                            continue;
                        }
                        
                        // 确保获取正确位置的数据，防止数组越界
                        String category = parts[0].trim();
                        String product = parts[1].trim();
                        
                        // 尝试解析日期 - 假设日期在第3列
                        LocalDate date;
                        String dateStr = parts[2].trim();
                        
                        try {
                            if (dateStr.contains("/")) {
                                // 处理yyyy/MM/dd格式
                                String[] dateParts = dateStr.split("/");
                                int year = Integer.parseInt(dateParts[0]);
                                int month = Integer.parseInt(dateParts[1]);
                                int day = Integer.parseInt(dateParts[2]);
                                date = LocalDate.of(year, month, day);
                            } else {
                                // 标准ISO格式yyyy-MM-dd
                                date = LocalDate.parse(dateStr);
                            }
                        } catch (Exception e) {
                            continue;
                        }
                        
                        // 获取时间和价格
                        String timeHour = parts[3].trim();
                        
                        double price;
                        try {
                            price = Double.parseDouble(parts[4].trim());
                        } catch (NumberFormatException e) {
                            continue;
                        }
                        
                        String remark = parts.length > 5 ? parts[5].trim() : product;
                        
                        // 创建并添加条目
                        BillingEntry entry = new BillingEntry(category, product, price, date, timeHour);
                        entry.remarkProperty().set(remark);
                        billingData.add(entry);
                    } catch (Exception e) {
                        // 忽略解析错误，继续处理下一行
                    }
                }
                
                // 刷新表格并保存到CSV
                billingTable.setItems(billingData);
                billingTable.refresh();
                saveToCsvFile();
            } catch (Exception e) {
                // 忽略CSV读取错误
            }
        }
    }

    private void handleModifyRecord() {
        BillingEntry selectedEntry = billingTable.getSelectionModel().getSelectedItem();
        if (selectedEntry == null) {
            return;
        }

        Dialog<BillingEntry> dialog = new Dialog<>();
        dialog.setTitle("修改记录");
        dialog.setHeaderText(null); // 移除标题

        ButtonType saveButtonType = new ButtonType("保存", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField categoryField = new TextField(selectedEntry.categoryProperty().get());
        TextField productField = new TextField(selectedEntry.productProperty().get());
        TextField priceField = new TextField(String.valueOf(selectedEntry.priceProperty().get()));
        DatePicker datePicker = new DatePicker(selectedEntry.timeProperty().get());
        TextField timeField = new TextField(selectedEntry.timeHourProperty().get());
        TextField remarkField = new TextField(selectedEntry.remarkProperty().get());

        grid.add(new Label("Category:"), 0, 0);
        grid.add(categoryField, 1, 0);
        grid.add(new Label("Product:"), 0, 1);
        grid.add(productField, 1, 1);
        grid.add(new Label("Price:"), 0, 2);
        grid.add(priceField, 1, 2);
        grid.add(new Label("Date:"), 0, 3);
        grid.add(datePicker, 1, 3);
        grid.add(new Label("Time:"), 0, 4);
        grid.add(timeField, 1, 4);
        grid.add(new Label("Remark:"), 0, 5);
        grid.add(remarkField, 1, 5);

        dialog.getDialogPane().setContent(grid);

        Platform.runLater(() -> categoryField.requestFocus());

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    double price = Double.parseDouble(priceField.getText().trim());
                    selectedEntry.categoryProperty().set(categoryField.getText().trim());
                    selectedEntry.productProperty().set(productField.getText().trim());
                    selectedEntry.priceProperty().set(price);
                    selectedEntry.timeProperty().set(datePicker.getValue());
                    selectedEntry.timeHourProperty().set(timeField.getText().trim());
                    selectedEntry.remarkProperty().set(remarkField.getText().trim());

                    String formattedTime = datePicker.getValue().getYear() + "." +
                        String.format("%02d", datePicker.getValue().getMonthValue()) + "." +
                        String.format("%02d", datePicker.getValue().getDayOfMonth()) + " " +
                        timeField.getText().trim();
                    selectedEntry.formattedTimeProperty().set(formattedTime);

                    return selectedEntry;
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        });

        Optional<BillingEntry> result = dialog.showAndWait();
        result.ifPresent(entry -> {
            billingTable.refresh();
            // 同步到CSV文件
            saveToCsvFile();
        });
    }

    // 将数据保存到CSV文件
    private void saveToCsvFile() {
        try {
            String csvFilePath = "data/billing/billingEntries.csv";
            File csvFile = new File(csvFilePath);
            
            // 确保目录存在
            csvFile.getParentFile().mkdirs();
            
            StringBuilder content = new StringBuilder();
            content.append("Category,Product,Date,Time,Price,Remark\n");
            
            for (BillingEntry entry : billingData) {
                String line = String.format("%s,%s,%s,%s,%.2f,%s\n",
                    entry.categoryProperty().get(),
                    entry.productProperty().get(),
                    entry.timeProperty().get().toString(),
                    entry.timeHourProperty().get(),
                    entry.priceProperty().get(),
                    entry.remarkProperty().get());
                content.append(line);
            }
            
            java.io.FileWriter writer = new java.io.FileWriter(csvFile);
            writer.write(content.toString());
            writer.close();
        } catch (Exception e) {
            logger.severe("Failed to save CSV file: " + e.getMessage());
        }
    }

    public static class BillingEntry {
        private final StringProperty category = new SimpleStringProperty();
        private final StringProperty product = new SimpleStringProperty();
        private final DoubleProperty price = new SimpleDoubleProperty();
        private final ObjectProperty<LocalDate> time = new SimpleObjectProperty<>();
        private final StringProperty timeHour = new SimpleStringProperty();
        private final StringProperty formattedTime = new SimpleStringProperty();
        private final StringProperty remark = new SimpleStringProperty();
        private String entryId;

        public BillingEntry(String category, String product, double price, LocalDate time, String timeHour) {
            this.category.set(category);
            this.product.set(product);
            this.price.set(price);
            this.time.set(time);
            this.timeHour.set(timeHour);
            
            String formattedDateTime = time.getYear() + "." +
                    String.format("%02d", time.getMonthValue()) + "." +
                    String.format("%02d", time.getDayOfMonth()) + " " +
                    timeHour;
            this.formattedTime.set(formattedDateTime);
            
            this.remark.set(product);
        }

        public StringProperty categoryProperty() { return category; }
        public StringProperty productProperty() { return product; }
        public DoubleProperty priceProperty() { return price; }
        public ObjectProperty<LocalDate> timeProperty() { return time; }
        public StringProperty timeHourProperty() { return timeHour; }
        public StringProperty formattedTimeProperty() { return formattedTime; }
        public StringProperty remarkProperty() { return remark; }
        
        public String getEntryId() { return entryId; }
        public void setEntryId(String entryId) { this.entryId = entryId; }
        public String getCachedEntryId() { return entryId; }
    }
}