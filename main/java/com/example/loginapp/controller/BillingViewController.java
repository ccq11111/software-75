package com.example.software.view;

import com.example.software.api.*;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;

import java.io.File;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class BillingViewController {
    private static final Logger logger = LoggerFactory.getLogger(BillingViewController.class.getName());

    // UI 元素
    @FXML private Label usernameLabel; // Only needed for displaying username 用户名标签
    @FXML private Button addCsvButton;// 添加 CSV 按钮
    @FXML private TextField productField;// 产品输入框
    @FXML private TextField priceField; // 价格输入框
    @FXML private DatePicker timePicker; // 日期选择器
    @FXML private TextField remarkField; // 备注输入框
    @FXML private TextField categoryField;  // New category text field

    @FXML private Button resetButton;// 重置按钮
    @FXML private Button buildButton;// 添加记录按钮
    @FXML private Button inquireButton; // 查询按钮
    @FXML private Button deleteButton; // 删除按钮
    @FXML private Button modifyButton; // 修改按钮

    @FXML private TableView<BillingEntry> billingTable;// 表格
    @FXML private TableColumn<BillingEntry, String> categoryColumn; // 分类列
    @FXML private TableColumn<BillingEntry, String> productColumn;// 产品列
    @FXML private TableColumn<BillingEntry, String> timeColumn;// 时间列
    @FXML private TableColumn<BillingEntry, Double> priceColumn; // 价格列
    @FXML private TableColumn<BillingEntry, String> remarkColumn;// 备注列

    private final ObservableList<BillingEntry> billingData = FXCollections.observableArrayList(); // 账单数据

    private BillingService billingService; // 账单服务
    private String token; // API token

    private final String LivingCosts = "生活费用";
    private final String Communication = "通讯";
    private final String Salary = "工资";
    private final String Cosmetics = "化妆品";
    @FXML
    public void initialize() {
        // Configure table columns
        categoryColumn.setCellValueFactory(cellData -> cellData.getValue().categoryProperty());
        productColumn.setCellValueFactory(cellData -> cellData.getValue().productProperty());
        timeColumn.setCellValueFactory(cellData -> cellData.getValue().formattedTimeProperty());
        priceColumn.setCellValueFactory(cellData -> cellData.getValue().priceProperty().asObject());
        remarkColumn.setCellValueFactory(cellData -> cellData.getValue().remarkProperty());

        // Custom cell factory for price column to show positive/negative values with colors
        priceColumn.setCellFactory(column -> new TableCell<BillingEntry, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    // Format with ¥ symbol and set color based on value
                    String formattedPrice = String.format("%s¥%.2f", item >= 0 ? "+" : "-", Math.abs(item));
                    setText(formattedPrice);

                    if (item >= 0) {
                        setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #F44336; -fx-font-weight: bold;");
                    }
                }
            }
        });

        // Set default category
        categoryField.setText("生活费用");

        // Set current date as default for date picker
        timePicker.setValue(LocalDate.now());

        // Button handlers
        addCsvButton.setOnAction(event -> handleAddCsv());
        resetButton.setOnAction(event -> clearInputFields());
        buildButton.setOnAction(event -> handleAddRecord());
        inquireButton.setOnAction(event -> handleFuzzySearch());
        deleteButton.setOnAction(event -> handleDeleteRecord());
        modifyButton.setOnAction(event -> handleModifyRecord());

        // Allow multiple selection in table
        billingTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        
        // Listen for table selection changes
        billingTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            updateDeleteButtonState();
            updateModifyButtonState();
        });

        // Set table items
        billingTable.setItems(billingData);

        // 清空CSV文件（初始化数据）
//        clearBillingEntriesFile();
        // 允许选择多个账单
        billingTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        // 监听表格选择项变化，更新删除和修改按钮的状态
        billingTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            updateDeleteButtonState();
            updateModifyButtonState();
        });

        // 清空CSV文件（初始化数据）
//        clearBillingEntriesFile();

        // 尝试从 API 获取 token，如果没有则使用示例数据
        try {
            String factoryToken = ApiServiceFactory.getInstance().getToken();
            if (factoryToken != null && !factoryToken.isEmpty()) {
                setToken(factoryToken);
            } else {

            }
        } catch (Exception e) {

        }
        //从后端加载加载数据
        loadBillingData();
    }

    private void addSampleData() {
        // Add sample data to match the screenshot
//        billingData.add(new BillingEntry("Living costs", "Nov.utilities bill", -200.00, LocalDate.of(2024, 12, 24), "14:29"));
//        billingData.add(new BillingEntry("Communication", "Dad phone bill", -200.00, LocalDate.of(2024, 12, 23), "14:29"));
//        billingData.add(new BillingEntry("Salary", "Nov.salary", 18800.00, LocalDate.of(2024, 12, 21), "14:29"));
//        billingData.add(new BillingEntry("Cosmetics", "-", -300.00, LocalDate.of(2024, 12, 13), "14:29"));
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
            // Get input values
            String category = categoryField.getText().trim();
            String product = productField.getText().trim();
            String remark = remarkField.getText().trim();
            double price = Double.parseDouble(priceField.getText().trim());
            LocalDate date = timePicker.getValue();

            // Validate required fields
            if (category.isEmpty() || product.isEmpty() || date == null) {
                return;
            }

            // Use product name as remark if not provided
            if (remark.isEmpty()) {
                remark = product;
            }

            // Create and add billing entry
            BillingEntry uiEntry = new BillingEntry(category, product, price, date, LocalTime.now(), remark);
            billingData.add(uiEntry);

            // Save to API if service exists
            if (billingService != null) {
                try {
                    billingService.createEntry(
                            category, product, BigDecimal.valueOf(price), date, LocalTime.now(), remark
                    );
                } catch (ApiException e) {
                    e.printStackTrace();
                }
            }

            // Clear input fields
            clearInputFields();
        } catch (NumberFormatException e) {
            // Ignore price format errors
        }
    }
    // 查询功能
    private void handleFuzzySearch() {
        String searchTerm = productField.getText().trim().toLowerCase();
        if (searchTerm.isEmpty()) {
            showAlert("Search Error", "Please enter a search term");
            return;
        }

        // Create a filtered list for the search results
        ObservableList<BillingEntry> searchResults = FXCollections.observableArrayList();

        // Perform fuzzy search on product and remark fields
        for (BillingEntry entry : billingData) {
            if (entry.productProperty().get().toLowerCase().contains(searchTerm) ||
                entry.remarkProperty().get().toLowerCase().contains(searchTerm)) {
                searchResults.add(entry);
            }
        }

        // Update the table with search results
        if (searchResults.isEmpty()) {
            showInfo("Search Results", "No matching records found");
        } else {
            billingTable.setItems(searchResults);
            showInfo("Search Results", searchResults.size() + " matching records found");
        }
    }
    // 选择并添加CSV文件
    private void handleAddCsv() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择CSV文件");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );
        File selectedFile = fileChooser.showOpenDialog(billingTable.getScene().getWindow());
        if(selectedFile != null){
            try {
                billingService.importFromCsv(selectedFile);
                // 重新加载数据以确保与服务器同步
                loadBillingData();
            } catch (ApiException e) {
                throw new RuntimeException(e);
            }
        }
//        if (selectedFile != null) {
//            try {
//                List<String> lines = java.nio.file.Files.readAllLines(selectedFile.toPath(), java.nio.charset.StandardCharsets.UTF_8);
//
//                for (int i = 1; i < lines.size(); i++) {
//                    try {
//                        String line = lines.get(i);
//                        String[] parts = line.split(",");
//
//                        // 检查CSV格式是否有足够的列
//                        if (parts.length < 5) {
//                            continue;
//                        }
//
//                        // 确保获取正确位置的数据，防止数组越界
//                        String category = parts[0].trim();
//                        String product = parts[1].trim();
//
//                        // 尝试解析日期 - 假设日期在第3列
//                        LocalDate date;
//                        String dateStr = parts[2].trim();
//
//                        try {
//                            if (dateStr.contains("/")) {
//                                // 处理yyyy/MM/dd格式
//                                String[] dateParts = dateStr.split("/");
//                                int year = Integer.parseInt(dateParts[0]);
//                                int month = Integer.parseInt(dateParts[1]);
//                                int day = Integer.parseInt(dateParts[2]);
//                                date = LocalDate.of(year, month, day);
//                            } else {
//                                // 标准ISO格式yyyy-MM-dd
//                                date = LocalDate.parse(dateStr);
//                            }
//                        } catch (Exception e) {
//                            continue;
//                        }
//
//                        // 获取时间和价格
//                        String timeHour = parts[3].trim();
//                        LocalTime time = LocalTime.parse(timeHour);
//                        double price;
//                        try {
//                            price = Double.parseDouble(parts[4].trim());
//                        } catch (NumberFormatException e) {
//                            continue;
//                        }
//
//                        String remark = parts.length > 5 ? parts[5].trim() : product;
//
//                        // 创建并添加条目
//                        BillingEntry entry = new BillingEntry(category, product, price, date, time,remark);
////                        entry.remarkProperty().set(remark);
//                        billingData.add(entry);
//                    } catch (Exception e) {
//                        // 忽略解析错误，继续处理下一行
//                    }
//                }
//
//                // 刷新表格并保存到CSV
//                billingTable.setItems(billingData);
//                billingTable.refresh();
//                saveToCsvFile();
//            } catch (Exception e) {
//                // 忽略CSV读取错误
//            }
//        }

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
            logger.error("Failed to save CSV file: " + e.getMessage());
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

            try {
                BillingEntry billingEntry = result.get();
                billingService.updateEntry(billingEntry.getEntryId(), billingEntry.category.getValue(),billingEntry.productProperty().getValue(), BigDecimal.valueOf(billingEntry.priceProperty().getValue()),
                        billingEntry.timeProperty().getValue(),LocalTime.parse(billingEntry.timeHour.getValue()),billingEntry.remarkProperty().getValue());
                // 重新加载数据以确保与服务器同步
                loadBillingData();
            } catch (ApiException e) {
                throw new RuntimeException(e);
            }

//            billingTable.refresh();
//            // 同步到CSV文件
//            saveToCsvFile();
        });
    }

    // 删除选中的账单记录
    private void handleDeleteRecord() {
        ObservableList<BillingEntry> selectedEntries = billingTable.getSelectionModel().getSelectedItems();
        if (selectedEntries.isEmpty()) {
            showErrorAlert("请先选择一条记录");
            return;
        }
        BillingEntry selectedEntry = selectedEntries.get(0);
        // 显示确认对话框
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("确认删除");
        confirmDialog.setHeaderText("确认删除记录");
        confirmDialog.setContentText("您确定要删除这条记录吗？此操作无法撤销。");

        // 等待用户选择
        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            System.out.println("确认删除记录: " + selectedEntry.getEntryId());

            // 执行删除操作
            CompletableFuture.supplyAsync(() -> {
                try {
                    return billingService.deleteEntry(selectedEntry.getEntryId());
                } catch (ApiException e) {
                    System.out.println("删除记录时发生错误: " + e.getMessage());
                    e.printStackTrace();
                    return new ApiResponse(false, "删除失败: " + e.getMessage());
                }
            }).thenAccept(response -> {
                System.out.println("删除响应: " + response.isSuccess() + ", " + response.getMessage());

                // 更新UI
                Platform.runLater(() -> {
                    setLoading(false);

                    if (response.isSuccess()) {
                        // 从本地列表移除记录
                        billingData.remove(selectedEntry);
                        // 重新加载数据以确保与服务器同步
                        loadBillingData();

                        // 提示用户
                        showSuccessAlert("记录已成功删除");
                    } else {
                        showErrorAlert("删除失败: " + response.getMessage());
                    }
                });
            }).exceptionally(ex -> {
                System.out.println("删除操作异常: " + ex.getMessage());
                ex.printStackTrace();

                Platform.runLater(() -> {
                    setLoading(false);
                    showErrorAlert("删除失败: " + ex.getMessage());
                });

                return null;
            });
        } else {
            setLoading(false);
            System.out.println("用户取消了删除操作");
        }
        // 直接删除选中的记录，不显示确认对话框
//        deleteSelectedEntries(selectedEntries);
    }
    /**
     * 加载账单数据
     */
    public void loadBillingData() {
        // 异步加载账单数据
        CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("开始从后端加载账单数据");
                return billingService.getEntries(null, null, null, null);
            } catch (ApiException e) {
                System.out.println("加载账单数据失败: " + e.getMessage());
                e.printStackTrace();
                throw new CompletionException(e);
            }
        }).thenAccept(entries -> {
            Platform.runLater(() -> {
                System.out.println("加载到 " + entries.size() + " 条账单记录");
                billingData.clear();
                for (com.example.software.api.BillingEntry billingEntry : entries) {
                    BillingEntry bill = new BillingEntry(billingEntry.getCategory(),billingEntry.getProduct(),billingEntry.getPrice().doubleValue(),billingEntry.getDate(),
                            billingEntry.getTime(),billingEntry.getRemark());
                    billingData.add(bill);
                }
                setLoading(false);
            });
        }).exceptionally(ex -> {
            System.out.println("加载账单数据异常: " + ex.getMessage());
            ex.printStackTrace();

            Platform.runLater(() -> {
                setLoading(false);
                showErrorAlert("加载账单数据失败: " + ex.getMessage());
            });

            return null;
        });
    }
    private void showSuccessAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("成功");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("错误");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    private void setLoading(boolean loading) {
        // 可以显示/隐藏加载指示器，或者禁用/启用按钮
//        refreshDataButton.setDisable(loading); // 加载时禁用按钮
        // 其他控件也可以在加载时禁用
        addCsvButton.setDisable(loading);
        buildButton.setDisable(loading);
        inquireButton.setDisable(loading);
        deleteButton.setDisable(loading);
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
    // 清空输入框
    private void clearInputFields() {
        productField.clear();
        priceField.clear();
        categoryField.setText("生活费用");  // Reset to default category
        timePicker.setValue(LocalDate.now());
        remarkField.clear();
        // Reset table to show all data after a search
        billingTable.setItems(billingData);
    }
    // 弹出错误提示
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
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
            logger.error("Failed to clear billing entries file: " + e.getMessage());
        }
    }
    // Navigation methods removed as they are now handled by BaseViewController

    // Store username as a field since we don't have the label in the content view
    private String username;

    public void setUsername(String username) {
        this.username = username;
        // Only set the label text if the label exists
        if (usernameLabel != null) {
            usernameLabel.setText(username);
        }
    }

    public String getUsername() {
        return username;
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
        public BillingEntry(String category, String product, double price, LocalDate date,LocalTime time,String remark) {
            this.category.set(category);
            this.product.set(product);
            this.price.set(price);
            this.time.set(date);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            String formattedTime = time.format(formatter);
            this.timeHour.set(formattedTime);

            String formattedDateTime = date.getYear() + "." +
                    String.format("%02d", date.getMonthValue()) + "." +
                    String.format("%02d", date.getDayOfMonth()) + " " +
                    timeHour.getValue();
            this.formattedTime.set(formattedDateTime);

            this.remark.set(remark);
            //可能为: date_category_product
            String entryId = date.getYear() + "-" +
                    String.format("%02d", date.getMonthValue()) + "-" +
                    String.format("%02d", date.getDayOfMonth()) + "_" +  category  + "_" + product;
            this.entryId = URLEncoder.encode(entryId, StandardCharsets.UTF_8);
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