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

import org.apache.commons.lang3.ObjectUtils.Null;

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
import com.example.loginapp.api.ApiServiceFactory;//连接api后端
import com.example.loginapp.api.ApiResponse;

public class BillingViewController {
    
    private static final Logger logger = Logger.getLogger(BillingViewController.class.getName());
    
    // UI 元素
    @FXML private Label usernameLabel; // 用户名标签
    @FXML private Button addCsvButton; // 添加 CSV 按钮
    @FXML private TextField categoryField; // 分类输入框
    @FXML private TextField productField; // 产品输入框
    @FXML private TextField priceField; // 价格输入框
    @FXML private TextField remarkField; // 备注输入框
    @FXML private DatePicker timePicker; // 日期选择器

    @FXML private Button resetButton; // 重置按钮
    @FXML private Button buildButton; // 添加记录按钮
    @FXML private Button inquireButton; // 查询按钮
    @FXML private Button refreshButton; // 刷新按钮
    @FXML private Button deleteButton; // 删除按钮
    @FXML private Button modifyButton; // 修改按钮

    // 表格相关
    @FXML private TableView<BillingEntry> billingTable; // 表格
    @FXML private TableColumn<BillingEntry, String> categoryColumn; // 分类列
    @FXML private TableColumn<BillingEntry, String> productColumn; // 产品列
    @FXML private TableColumn<BillingEntry, String> timeColumn; // 时间列
    @FXML private TableColumn<BillingEntry, Double> priceColumn; // 价格列
    @FXML private TableColumn<BillingEntry, String> remarkColumn; // 备注列

    private final ObservableList<BillingEntry> billingData = FXCollections.observableArrayList(); // 账单数据

    private BillingService billingService; // 账单服务
    private String token; // API token
    private String username; // 用户名

    // 初始化方法
    @FXML
    public void initialize() {
        // 初始化可编辑列
        initializeEditableColumns();

        // 允许选择多个账单
        billingTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        
        // 监听表格选择项变化，更新删除和修改按钮的状态
        billingTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            updateDeleteButtonState();
            updateModifyButtonState();
        });

        // 设置价格列显示格式（正负颜色和符号）
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

        // 设置分类列显示方式
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

        // 设置账单数据
        billingTable.setItems(billingData);

        // 设置时间选择器的默认值为今天
        timePicker.setValue(LocalDate.now());

        // 清空CSV文件（初始化数据）
        clearBillingEntriesFile();

        // 尝试从 API 获取 token，如果没有则使用示例数据
        try {
            String factoryToken = ApiServiceFactory.getInstance().getToken();
            if (factoryToken != null && !factoryToken.isEmpty()) {
                setToken(factoryToken);
            } else {
                
            }
        } catch (Exception e) {
            
        }

        // 按钮事件绑定--各个功能按钮
        addCsvButton.setOnAction(event -> handleAddCsv());
        resetButton.setOnAction(event -> clearInputFields());
        buildButton.setOnAction(event -> handleAddRecord());
        inquireButton.setOnAction(event -> handleFuzzySearch());
        modifyButton.setOnAction(event -> handleModifyRecord());
        deleteButton.setOnAction(event -> handleDeleteRecords());

        // 默认禁用修改和删除按钮
        modifyButton.setDisable(true);
        updateDeleteButtonState();
    }

    // 设置API的token
    public void setToken(String token) {
        this.token = token;
        this.billingService = new RealBillingService(token);
    }

    // 判断是否需要刷新token
    private boolean needTokenRefresh() {
        return token == null || token.isEmpty();
    }
    
    // 添加账单记录
    private void handleAddRecord() {
        if (needTokenRefresh()) {
            return;
        }
        
        try {
            // 获取用户输入的账单信息
            String category = categoryField.getText().trim();
            String product = productField.getText().trim();
            String remark = remarkField.getText().trim();
            double price = Double.parseDouble(priceField.getText().trim());
            LocalDate date = timePicker.getValue();

            // 如果分类、产品或日期为空，则不添加
            if (category.isEmpty() || product.isEmpty() || date == null) {
                return;
            }

            // 如果备注为空，默认为产品名
            if (remark.isEmpty()) {
                remark = null;
            }

            // 创建并添加账单条目
            BillingEntry uiEntry = new BillingEntry(category, product, price, date, "14:29");
            uiEntry.remarkProperty().set(remark);
            billingData.add(uiEntry);
            
            // 如果服务存在，则将数据保存到API
            if (billingService != null) {
                    BigDecimal bdPrice = BigDecimal.valueOf(price);
                try {
                    billingService.createEntry(
                        category, product, bdPrice, date, LocalTime.now(), remark
                    );
                } catch (ApiException e) {
                    // 处理API异常
                }
            }

            // 清空输入框
            clearInputFields();
            saveToCsvFile();
        } catch (NumberFormatException e) {
            // 价格格式错误，忽略
        }
    }

    // 查询功能
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
    
    // 重置查询功能
    private void resetSearch() {
        productField.clear();
        billingData.clear();
        tryReadFromCsvFile();
        inquireButton.setText("Inquire");
        inquireButton.setOnAction(e -> handleFuzzySearch());
    }
    
    // 从CSV文件读取数据 查询
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

    // 清空输入框
    private void clearInputFields() {
        categoryField.clear();
        productField.clear();
        priceField.clear();
        remarkField.clear();
        timePicker.setValue(LocalDate.now());
        billingTable.setItems(billingData);
    }

    // 弹出错误提示
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

    // 删除选中的账单记录
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

    // 更新删除按钮状态
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

    // 更新修改按钮状态
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

    // 初始化可编辑列
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

    // 清空账单条目文件
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

    // 选择并添加CSV文件
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

    // 修改账单记录
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

        // 设置每个输入框的默认值
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

        // 处理保存逻辑
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

        // 处理对话框返回值
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

    // BillingEntry 类：用于存储单条账单记录的数据
    public static class BillingEntry {
        private final StringProperty category = new SimpleStringProperty();
        private final StringProperty product = new SimpleStringProperty();
        private final DoubleProperty price = new SimpleDoubleProperty();
        private final ObjectProperty<LocalDate> time = new SimpleObjectProperty<>();
        private final StringProperty timeHour = new SimpleStringProperty();
        private final StringProperty formattedTime = new SimpleStringProperty();
        private final StringProperty remark = new SimpleStringProperty();
        private String entryId;

        // 构造函数
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

        // getter 和 setter 方法
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
