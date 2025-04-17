package com.example.loginapp;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.*;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.time.LocalTime;
import java.math.BigDecimal;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.Map;
import java.time.format.DateTimeFormatter;
import javafx.scene.control.SelectionMode;
import java.util.ArrayList;
import javafx.stage.FileChooser;
import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import javafx.application.Platform;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.HBox;
import java.util.function.BiConsumer;
import java.util.function.Function;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import java.util.Set;
import java.util.HashSet;
import java.util.Optional;

import com.example.loginapp.api.BillingService;
import com.example.loginapp.api.RealBillingService;
import com.example.loginapp.api.ApiException;
import com.example.loginapp.api.BillingEntryResponse;
import com.example.loginapp.api.ApiServiceFactory;
import com.example.loginapp.api.ApiResponse;
import com.example.loginapp.api.AuthService;
import com.example.loginapp.api.AuthResponse;
import com.example.loginapp.api.ImportResponse;

public class BillingViewController {
    
    private static final Logger logger = Logger.getLogger(BillingViewController.class.getName());
    
    @FXML private Label usernameLabel; // Only needed for displaying username
    @FXML private Button addCsvButton;

    @FXML private TextField categoryField; // 新增类别输入框
    @FXML private TextField productField;
    @FXML private TextField priceField;
    @FXML private TextField remarkField; // 新增备注输入框
    @FXML private DatePicker timePicker;

    @FXML private Button resetButton;
    @FXML private Button buildButton;
    @FXML private Button inquireButton;
    @FXML private Button refreshButton; // 新增刷新按钮
    @FXML private Button deleteButton; // 新增删除按钮

    @FXML private TableView<BillingEntry> billingTable;
    @FXML private TableColumn<BillingEntry, String> categoryColumn;
    @FXML private TableColumn<BillingEntry, String> productColumn;
    @FXML private TableColumn<BillingEntry, String> timeColumn;
    @FXML private TableColumn<BillingEntry, Double> priceColumn;
    @FXML private TableColumn<BillingEntry, String> remarkColumn;

    private final ObservableList<BillingEntry> billingData = FXCollections.observableArrayList();
    
    // 添加API服务
    private BillingService billingService;
    private String token;

    @FXML
    public void initialize() {
        logger.info("初始化BillingViewController");
        
        // 直接清空CSV文件，无需询问
        Platform.runLater(() -> {
            clearBillingEntriesFile();
        });
        
        // 配置表格列
        categoryColumn.setCellValueFactory(cellData -> cellData.getValue().categoryProperty());
        productColumn.setCellValueFactory(cellData -> cellData.getValue().productProperty());
        timeColumn.setCellValueFactory(cellData -> cellData.getValue().formattedTimeProperty());
        priceColumn.setCellValueFactory(cellData -> cellData.getValue().priceProperty().asObject());
        remarkColumn.setCellValueFactory(cellData -> cellData.getValue().remarkProperty());

        // 允许表格进行多选
        billingTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        
        // 添加表格选择监听器，在选择变化时更新删除按钮状态
        billingTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            updateDeleteButtonState();
        });

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

        // Custom cell factory for category column to show icons
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

        // Set current date as default for date picker
        timePicker.setValue(LocalDate.now());

        // 设置提示信息
        inquireButton.setTooltip(new Tooltip("输入关键词搜索或留空刷新所有数据"));

        // 如果没有刷新按钮，创建一个
        refreshButton = null; // 确保这个引用为null
        
        // 设置删除按钮的提示和事件
        if (deleteButton != null) {
            deleteButton.setTooltip(new Tooltip("删除选中的账单记录"));
            deleteButton.setOnAction(event -> handleDeleteRecords());
        }

        // 尝试从ApiServiceFactory获取token
        try {
            String factoryToken = ApiServiceFactory.getInstance().getToken();
            if (factoryToken != null && !factoryToken.isEmpty()) {
                logger.info("从ApiServiceFactory获取到token: " + factoryToken);
                setToken(factoryToken);
            } else {
                logger.warning("ApiServiceFactory中没有token");
        addSampleData();
            }
        } catch (Exception e) {
            logger.severe("获取token失败: " + e.getMessage());
        addSampleData();
        }

        // Button handlers
        addCsvButton.setOnAction(event -> handleAddCsv());
        resetButton.setOnAction(event -> clearInputFields());
        buildButton.setOnAction(event -> handleAddRecord()); // Build button adds a record
        inquireButton.setOnAction(event -> handleFuzzySearch()); // Inquire button performs fuzzy search
        
        // 设置删除按钮初始状态为禁用(没有选中记录时)
        updateDeleteButtonState();

        // We no longer need menu button handlers as they are handled by BaseViewController

        initializeEditableColumns();

        // 延迟执行以确保场景完全加载
        Platform.runLater(() -> {
            // 删除所有现有的刷新按钮
            removeAllRefreshButtons();
            
            // 创建新的按钮在顶部
            createCircleRefreshButton();
        });
    }

    // 设置token和初始化API服务
    public void setToken(String token) {
        logger.info("设置token: " + token);
        this.token = token;
        this.billingService = new RealBillingService(token);
        
        // 不再自动加载后端数据
        // loadBillingData();
    }
    
    // 从后端加载账单数据
    private void loadBillingData() {
        logger.info("开始从后端加载账单数据");
        
        if (needTokenRefresh()) {
            logger.info("Token需要刷新，但在当前方法中无法处理");
            showInfo("Token过期", "请点击刷新按钮或重新登录后再尝试加载数据");
            return;
        }
        
        try {
            logger.info("调用billingService.getEntries开始");
            List<com.example.loginapp.api.BillingEntry> entries = billingService.getEntries(null, null, null, null);
            logger.info("调用billingService.getEntries结束，获取到 " + entries.size() + " 条记录");
            for (com.example.loginapp.api.BillingEntry entry : entries) {
                logger.info("记录: " + entry.getCategory() + ", " + entry.getProduct() + ", " + entry.getPrice() + ", ID: " + entry.getEntryId());
            }
            
            // 转换为UI模型并添加到表格
            billingData.clear();
            for (com.example.loginapp.api.BillingEntry entry : entries) {
                BillingEntry uiEntry = new BillingEntry(
                    entry.getCategory(),
                    entry.getProduct(),
                    entry.getPrice().doubleValue(),
                    entry.getDate(),
                    entry.getFormattedTime()
                );
                
                // 设置备注和entryId
                uiEntry.remarkProperty().set(entry.getRemark());
                uiEntry.setEntryId(entry.getEntryId());
                
                billingData.add(uiEntry);
            }
            
            // 如果没有数据，尝试从CSV文件直接读取
            if (billingData.isEmpty()) {
                logger.warning("从后端加载的数据为空，尝试直接从CSV文件读取");
                tryReadFromCsvFile();
            }
            
            // 关键部分：强制更新表格
            billingTable.setItems(null);  // 先清空表格
            billingTable.setItems(billingData);  // 再设置数据
            billingTable.refresh();  // 强制刷新表格显示
            
            logger.info("表格数据加载完成，共加载 " + billingData.size() + " 条记录");
            
        } catch (ApiException e) {
            logger.warning("加载账单数据失败: " + e.getMessage());
            
            // 尝试直接从CSV文件读取数据
            tryReadFromCsvFile();
        }
    }

    // 检查token是否需要刷新
    private boolean needTokenRefresh() {
        // 如果是测试用户，不需要刷新
        if ("mock-token-test-account".equals(token)) {
            return false;
        }
        
        // 简单地检查token是否存在
        return token == null || token.isEmpty();
    }
    
    // 刷新token
    private void refreshToken() {
        logger.info("尝试刷新token");
        try {
            // 获取用户名
            String username = getUsername();
            if (username == null || username.isEmpty()) {
                logger.warning("无法刷新token：用户名为空");
                showAlert("Error", "Cannot refresh token: Username is empty");
                return;
            }
            
            // 尝试调用登录API
            AuthService authService = ApiServiceFactory.getInstance().getAuthService();
            // 注意：这里需要用户输入密码，但我们没有保存密码，因此可能需要弹出密码输入框
            // 这里简化处理，假设用户名和密码相同
            try {
                TextInputDialog dialog = new TextInputDialog();
                dialog.setTitle("刷新令牌");
                dialog.setHeaderText("为了安全验证，请输入您的密码：");
                dialog.setContentText("密码:");
                
                dialog.showAndWait().ifPresent(password -> {
                    try {
                        AuthResponse response = authService.login(username, password);
                        if (response != null && response.getToken() != null) {
                            String newToken = response.getToken();
                            logger.info("成功刷新token: " + newToken);
                            
                            // 更新token
                            this.token = newToken;
                            this.billingService = new RealBillingService(newToken);
                            
                            // 更新ApiServiceFactory中的token
                            ApiServiceFactory.getInstance().setUserContext(username, newToken);
                            
                            showInfo("成功", "令牌已刷新");
                            loadBillingData();
                        } else {
                            logger.warning("刷新token失败：响应为空或无token");
                            showAlert("Error", "Failed to refresh token: Empty response or no token");
                        }
                    } catch (ApiException e) {
                        logger.severe("刷新token失败: " + e.getMessage());
                        showAlert("Error", "Failed to refresh token: " + e.getMessage());
                    }
                });
            } catch (Exception e) {
                logger.severe("显示密码输入框失败: " + e.getMessage());
                showAlert("Error", "Failed to show password dialog: " + e.getMessage());
            }
        } catch (Exception e) {
            logger.severe("刷新token过程中发生异常: " + e.getMessage());
            showAlert("Error", "Exception during token refresh: " + e.getMessage());
        }
    }

    private void addSampleData() {
        // 只有在没有API服务时才添加示例数据
        if (billingService == null) {
            logger.info("添加示例数据");
        billingData.add(new BillingEntry("Living costs", "Nov.utilities bill", -200.00, LocalDate.of(2024, 12, 24), "14:29"));
        billingData.add(new BillingEntry("Communication", "Dad phone bill", -200.00, LocalDate.of(2024, 12, 23), "14:29"));
        billingData.add(new BillingEntry("Salary", "Nov.salary", 18800.00, LocalDate.of(2024, 12, 21), "14:29"));
        billingData.add(new BillingEntry("Cosmetics", "-", -300.00, LocalDate.of(2024, 12, 13), "14:29"));
        }
    }

    private void handleAddRecord() {
        logger.info("开始添加账单记录");
        
        // 在添加记录前检查token
        if (needTokenRefresh()) {
            logger.info("Token需要刷新，提示用户");
            showAlert("需要刷新", "您的令牌可能已过期，请点击刷新按钮或重新登录后再尝试");
            return;
        }
        
        try {
            String category = categoryField.getText().trim();
            String product = productField.getText().trim();
            String remark = remarkField.getText().trim(); // 获取备注内容
            double price = Double.parseDouble(priceField.getText().trim());
            LocalDate date = timePicker.getValue();

            if (category.isEmpty() || product.isEmpty() || date == null) {
                logger.warning("表单数据不完整");
                showAlert("Error", "Please fill in all fields");
                return;
            }

            // 如果没有输入备注，使用产品名作为备注
            if (remark.isEmpty()) {
                remark = product;
            }

            // 添加到UI表格
            BillingEntry uiEntry = new BillingEntry(category, product, price, date, "14:29");
            uiEntry.remarkProperty().set(remark); // 设置备注
            billingData.add(uiEntry);
            logger.info("已添加到UI表格: " + category + ", " + product + ", " + price + ", 备注: " + remark);
            
            // 如果API服务可用，保存到后端
            if (billingService != null) {
                try {
                    // 使用格式化的时间，不带毫秒
                    LocalTime time = LocalTime.now();
                    // 格式化为HH:mm格式
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
                    String formattedTimeStr = time.format(formatter);
                    LocalTime formattedTime = LocalTime.parse(formattedTimeStr, formatter);
                    
                    BigDecimal bdPrice = BigDecimal.valueOf(price);
                    
                    logger.info("开始调用后端API保存账单");
                    logger.info("使用token: " + this.token);
                    logger.info("参数: category=" + category + ", product=" + product + 
                               ", price=" + bdPrice + ", date=" + date + ", time=" + formattedTime + 
                               ", remark=" + remark);
                    
                    // 打印更详细的调试信息
                    logger.info("date.toString()输出: " + date.toString());
                    logger.info("time.toString()输出: " + formattedTime.toString());
                    
                    // 尝试先发送一个极简的请求测试
                    try {
                        Map<String, Object> simpleRequest = new HashMap<>();
                        simpleRequest.put("category", category);
                        simpleRequest.put("product", product);
                        simpleRequest.put("price", bdPrice);
                        simpleRequest.put("date", date);
                        simpleRequest.put("remark", remark);
                        
                        logger.info("发送简化请求测试: " + simpleRequest);
                        // 可以在这里添加发送简化请求的代码，暂时注释掉
                    } catch (Exception e) {
                        logger.severe("简化请求测试失败: " + e.getMessage());
                    }
                    
                    // 调用API保存账单
                    BillingEntryResponse response = billingService.createEntry(
                        category, product, bdPrice, date, formattedTime, remark // 使用用户输入的备注
                    );
                    
                    if (response != null && response.isSuccess()) {
                        logger.info("保存到后端成功: " + response);
                        showInfo("Success", "Billing entry saved to backend successfully");
                    } else {
                        logger.warning("保存到后端返回失败结果: " + (response != null ? response.getMessage() : "null response"));
                        if (response != null && response.getError() != null) {
                            logger.warning("错误详情: " + response.getError().getCode() + " - " + response.getError().getMessage());
                        }
                        showAlert("API Error", "Failed to save to backend: " + (response != null ? response.getMessage() : "null response"));
                    }
                } catch (ApiException e) {
                    logger.severe("调用API保存账单失败: " + e.getMessage());
                    logger.severe("异常堆栈: " + e);
                    if (e.getCause() != null) {
                        logger.severe("原始异常: " + e.getCause().getMessage());
                        e.getCause().printStackTrace();
                    }
                    
                    // 尝试一次更直接的简单请求，作为最后的失败恢复尝试
                    try {
                        logger.info("尝试最后的失败恢复措施...");
                        String testEndpoint = "http://127.0.0.1:8080/billing/debug";
                        logger.info("测试连接到: " + testEndpoint);
                        
                        // 此处添加测试连接代码（可选）...
                        
                    } catch (Exception testEx) {
                        logger.severe("测试连接也失败: " + testEx.getMessage());
                    }
                    
                    showAlert("API Error", "Failed to save to backend: " + e.getMessage());
                }
            } else {
                logger.warning("billingService为空，无法保存到后端");
                showInfo("Local Mode", "Running in local mode, data not saved to backend");
            }
            
            clearInputFields();
        } catch (NumberFormatException e) {
            logger.warning("价格格式错误: " + e.getMessage());
            showAlert("Error", "Please enter a valid price");
        }
    }

    private void handleFuzzySearch() {
        String searchTerm = productField.getText().trim();
        String lowerSearchTerm = searchTerm.toLowerCase(); // 仅用于备注模糊查询
        
        // 如果搜索词为空，刷新所有数据
        if (searchTerm.isEmpty()) {
            logger.info("搜索词为空，显示所有数据");
            // 清空现有数据并从CSV文件重新加载
            billingData.clear();
            tryReadFromCsvFile();
            return;
        }

        logger.info("使用本地搜索: " + searchTerm);
        // 直接从当前数据中搜索，如果数据为空先加载
        if (billingData.isEmpty()) {
            tryReadFromCsvFile();
        }
        
        // 本地精确匹配搜索
        ObservableList<BillingEntry> searchResults = FXCollections.observableArrayList();
        for (BillingEntry entry : billingData) {
            // 产品名称必须完全匹配 (不区分大小写)
            if (entry.productProperty().get().equalsIgnoreCase(searchTerm) ||
                // 备注可以继续使用模糊匹配
                entry.remarkProperty().get().toLowerCase().contains(lowerSearchTerm)) {
                searchResults.add(entry);
            }
        }

        if (searchResults.isEmpty()) {
            logger.info("本地搜索无结果");
            showInfo("搜索结果", "未找到匹配的记录");
        } else {
            logger.info("本地搜索找到 " + searchResults.size() + " 条结果");
            billingTable.setItems(searchResults); // 只显示搜索结果
            
            // 修改搜索按钮文本，变成"显示全部"
            inquireButton.setText("显示全部");
            inquireButton.setOnAction(e -> resetSearch());
            // 添加提示
            inquireButton.setTooltip(new Tooltip("点击显示所有账单"));
            
            showInfo("搜索结果", "找到 " + searchResults.size() + " 条匹配的记录");
        }
    }
    
    /**
     * 重置搜索，显示所有账单
     */
    private void resetSearch() {
        logger.info("重置搜索，显示所有账单");
        
        // 清空搜索字段
        productField.clear();
        
        // 重新加载CSV数据
        billingData.clear();
        tryReadFromCsvFile();
        
        // 重置按钮状态
        inquireButton.setText("查询");
        inquireButton.setOnAction(e -> handleFuzzySearch());
        inquireButton.setTooltip(new Tooltip("输入关键词搜索或留空刷新所有数据"));
    }
    
    /**
     * 尝试直接从CSV文件读取数据
     */
    private void tryReadFromCsvFile() {
        logger.info("尝试直接从CSV文件读取数据");
        
        try {
            // CSV文件路径
            String csvFilePath = "D:\\software-75-main\\data\\billing\\billingEntries.csv";
            java.io.File csvFile = new java.io.File(csvFilePath);
            
            if (!csvFile.exists()) {
                logger.warning("CSV文件不存在: " + csvFilePath);
                return;
            }
            
            logger.info("找到CSV文件: " + csvFilePath);
            
            // 读取CSV文件内容
            List<String> lines = java.nio.file.Files.readAllLines(csvFile.toPath(), java.nio.charset.StandardCharsets.UTF_8);
            logger.info("CSV文件共 " + lines.size() + " 行");
            
            if (lines.isEmpty()) {
                logger.warning("CSV文件为空");
                return;
            }
            
            // 清空现有数据
            billingData.clear();
            
            for (String line : lines) {
                logger.info("CSV行: " + line);
                
                try {
                    // 解析CSV行 (格式: 类别,产品,日期,时间,价格,备注)
                    String[] parts = line.split(",");
                    if (parts.length < 5) {
                        logger.warning("CSV行格式不正确: " + line);
                        continue;
                    }
                    
                    String category = parts[0];
                    String product = parts[1];
                    
                    // 解析日期 - 支持多种格式(YYYY/MM/DD 或 YYYY-MM-DD)
                    LocalDate date;
                    String dateStr = parts[2];
                    try {
                        // 处理YYYY/MM/DD格式
                        if (dateStr.contains("/")) {
                            String[] dateParts = dateStr.split("/");
                            if (dateParts.length == 3) {
                                int year = Integer.parseInt(dateParts[0]);
                                int month = Integer.parseInt(dateParts[1]);
                                int day = Integer.parseInt(dateParts[2]);
                                date = LocalDate.of(year, month, day);
                            } else {
                                logger.warning("无效的日期格式: " + dateStr);
                                date = LocalDate.now();
                            }
                        } else {
                            // 尝试标准格式 YYYY-MM-DD
                            date = LocalDate.parse(dateStr);
                        }
                        logger.info("成功解析日期: " + dateStr + " -> " + date);
                    } catch (Exception e) {
                        logger.warning("日期格式错误，使用当前日期: " + dateStr + ", 错误: " + e.getMessage());
                        date = LocalDate.now();
                    }
                    
                    // 解析时间
                    String timeStr = parts[3];
                    
                    // 解析价格
                    double price;
                    try {
                        price = Double.parseDouble(parts[4]);
                    } catch (NumberFormatException e) {
                        logger.warning("价格格式错误，默认为0: " + parts[4]);
                        price = 0.0;
                    }
                    
                    // 解析备注
                    String remark = parts.length > 5 ? parts[5] : product;
                    
                    // 创建账单条目
                    BillingEntry entry = new BillingEntry(category, product, price, date, timeStr);
                    entry.remarkProperty().set(remark);
                    
                    // 生成ID
                    String entryId = String.format("%s_%s_%s", 
                        date.toString(),
                        category.replaceAll("[\\s:,]+", "_"),
                        product.replaceAll("[\\s:,]+", "_"));
                    entry.setEntryId(entryId);
                    
                    // 添加到表格
                    billingData.add(entry);
                    logger.info("从CSV添加记录: " + category + ", " + product + ", " + price);
                    
                } catch (Exception e) {
                    logger.warning("解析CSV行失败: " + line + ", 错误: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            // 更新表格
            billingTable.setItems(null);
            billingTable.setItems(billingData);
            billingTable.refresh();
            
            logger.info("从CSV文件加载完成，共加载 " + billingData.size() + " 条记录");
            
        } catch (Exception e) {
            logger.severe("读取CSV文件失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void clearInputFields() {
        categoryField.clear();
        productField.clear();
        priceField.clear();
        remarkField.clear(); // 清除备注字段
        timePicker.setValue(LocalDate.now());
        billingTable.setItems(billingData);
    }

    private void showAlert(String title, String content) {
        logger.warning(title + ": " + content);
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showInfo(String title, String content) {
        logger.info(title + ": " + content);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
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

    /**
     * 处理删除记录操作
     */
    private void handleDeleteRecords() {
        logger.info("开始删除选中的记录");
        
        // 在删除记录前检查token
        if (needTokenRefresh()) {
            logger.info("Token需要刷新，提示用户");
            showAlert("需要刷新", "您的令牌可能已过期，请点击刷新按钮或重新登录后再尝试");
            return;
        }
        
        // 获取选中的记录
        ObservableList<BillingEntry> selectedEntries = billingTable.getSelectionModel().getSelectedItems();
        
        if (selectedEntries.isEmpty()) {
            logger.warning("没有选中任何记录");
            showAlert("Error", "请先选择要删除的记录");
            return;
        }
        
        // 确认是否删除
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("确认删除");
        confirmDialog.setHeaderText("您确定要删除选中的 " + selectedEntries.size() + " 条记录吗?");
        confirmDialog.setContentText("此操作无法撤销");
        
        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                deleteSelectedEntries(selectedEntries);
            }
        });
    }
    
    /**
     * 执行删除选中记录的操作
     */
    private void deleteSelectedEntries(ObservableList<BillingEntry> selectedEntries) {
        int totalCount = selectedEntries.size();
        int successCount = 0;
        int failCount = 0;
        
        // 复制一份选中的记录，避免ConcurrentModificationException
        List<BillingEntry> entriesToDelete = new ArrayList<>(selectedEntries);
        
        for (BillingEntry entry : entriesToDelete) {
            try {
                // 尝试使用已缓存的ID
                String entryId = entry.getCachedEntryId();
                logger.info("原始缓存ID: " + entryId);
                
                // 如果ID为空，尝试从后端获取匹配的ID
                if (entryId == null || entryId.isEmpty()) {
                    logger.info("记录没有关联的ID，尝试从后端查询");
                    
                    try {
                        // 使用日期+产品+类别+价格组合查询
                        List<com.example.loginapp.api.BillingEntry> backendEntries = 
                            billingService.getEntries(entry.timeProperty().get(), null, entry.categoryProperty().get(), entry.productProperty().get());
                        
                        logger.info("查询到 " + (backendEntries != null ? backendEntries.size() : 0) + " 条匹配记录");
                        
                        // 查找匹配的记录
                        if (backendEntries != null) {
                            for (com.example.loginapp.api.BillingEntry backendEntry : backendEntries) {
                                logger.info("比较后端记录: category=" + backendEntry.getCategory() + 
                                           ", product=" + backendEntry.getProduct() + 
                                           ", date=" + backendEntry.getDate());
                                
                                if (backendEntry.getCategory().equals(entry.categoryProperty().get()) &&
                                    backendEntry.getProduct().equals(entry.productProperty().get()) &&
                                    backendEntry.getDate().equals(entry.timeProperty().get())) {
                                    
                                    entryId = backendEntry.getEntryId();
                                    logger.info("找到匹配的后端ID: " + entryId);
                                    break;
                                }
                            }
                        }
                    } catch (ApiException e) {
                        logger.warning("查询后端ID失败: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
                
                // 如果仍然没有ID，生成一个基于记录特征的ID
                if (entryId == null || entryId.isEmpty()) {
                    // 创建一个基于记录特征的ID，格式为: date_category_product
                    String generatedId = String.format("%s_%s_%s", 
                        entry.timeProperty().get().toString(),
                        entry.categoryProperty().get(),
                        entry.productProperty().get());
                    logger.info("使用生成的ID: " + generatedId);
                    entryId = generatedId;
                }
                
                // 确保entryId的格式正确，要求包含至少一个下划线
                if (!entryId.contains("_")) {
                    String formattedId = String.format("%s_%s_%s", 
                        entry.timeProperty().get().toString(),
                        entry.categoryProperty().get(),
                        entry.productProperty().get());
                    logger.info("entryId格式不正确，重新格式化: " + entryId + " -> " + formattedId);
                    entryId = formattedId;
                }
                
                // 后端API调用
                logger.info("尝试删除记录ID: " + entryId);
                
                // 调用API删除记录
                ApiResponse response = billingService.deleteEntry(entryId);
                
                if (response != null && response.isSuccess()) {
                    logger.info("成功删除记录: " + entryId);
                    successCount++;
                    
                    // 立即从前端集合中删除
                    billingData.remove(entry);
                } else {
                    logger.warning("删除记录失败: " + entryId);
                    logger.warning("失败原因: " + (response != null ? response.getMessage() : "unknown"));
                    failCount++;
                }
            } catch (ApiException e) {
                logger.severe("删除记录时发生异常: " + e.getMessage());
                e.printStackTrace();
                failCount++;
            }
        }
        
        // 刷新UI表格
        billingTable.refresh();
        
        // 如果有成功删除的记录，直接写入CSV文件
        if (successCount > 0) {
            tryWriteManualCsvFile();
            
            // 直接从CSV文件重新加载数据
            logger.info("从CSV文件重新加载数据");
            billingData.clear(); // 清空当前数据
            tryReadFromCsvFile(); // 从CSV文件读取最新数据
        }
        
        // 显示结果
        String message = String.format("操作完成: %d条记录已删除, %d条失败", successCount, failCount);
        logger.info(message);
        
        if (successCount > 0 && failCount == 0) {
            showInfo("删除成功", message);
        } else if (successCount > 0 && failCount > 0) {
            showAlert("部分成功", message);
        } else {
            showAlert("删除失败", message);
        }
    }

    /**
     * 更新删除按钮状态
     */
    private void updateDeleteButtonState() {
        if (deleteButton != null) {
            boolean hasSelection = !billingTable.getSelectionModel().isEmpty();
            deleteButton.setDisable(!hasSelection);
            
            // 更新工具提示
            int selectedCount = billingTable.getSelectionModel().getSelectedItems().size();
            if (selectedCount > 0) {
                deleteButton.setTooltip(new Tooltip("删除选中的 " + selectedCount + " 条记录"));
            } else {
                deleteButton.setTooltip(new Tooltip("选择记录后可删除"));
            }
        }
    }

    private void setLoading(boolean loading) {
        // 实现加载指示器逻辑
        logger.info("设置加载状态: " + loading);
        
        // 这里可以添加UI加载指示器的代码
        // 例如，如果有一个加载指示器控件:
        // loadingIndicator.setVisible(loading);
    }

    @FXML
    private void addRefreshButton() {
        Button refreshDataButton = new Button("刷新数据");
        refreshDataButton.setOnAction(event -> {
            logger.info("手动刷新账单数据");
            loadBillingData();
        });
        
        // 添加到界面上，例如放在addCsvButton旁边
        HBox buttonBox = (HBox) addCsvButton.getParent();
        if (buttonBox != null) {
            buttonBox.getChildren().add(refreshDataButton);
        }
    }

    private void initializeEditableColumns() {
        // 设置类别列可编辑
        categoryColumn.setCellFactory(column -> new EditableCell<>(
            cellData -> cellData.getValue().categoryProperty(),
            (entry, newValue) -> {
                entry.categoryProperty().set(newValue);
                updateBillingEntry(entry);
            }
        ));

        // 设置产品列可编辑
        productColumn.setCellFactory(column -> new EditableCell<>(
            cellData -> cellData.getValue().productProperty(),
            (entry, newValue) -> {
                entry.productProperty().set(newValue);
                updateBillingEntry(entry);
            }
        ));

        // 设置价格列可编辑
        priceColumn.setCellFactory(column -> new EditablePriceCell(
            cellData -> cellData.getValue().priceProperty(),
            (entry, newValue) -> {
                entry.priceProperty().set(newValue);
                updateBillingEntry(entry);
            }
        ));

        // 设置备注列可编辑
        remarkColumn.setCellFactory(column -> new EditableCell<>(
            cellData -> cellData.getValue().remarkProperty(),
            (entry, newValue) -> {
                entry.remarkProperty().set(newValue);
                updateBillingEntry(entry);
            }
        ));
    }

    // 可编辑单元格的实现
    private class EditableCell<T> extends TableCell<BillingEntry, String> {
        private final Function<TableColumn.CellDataFeatures<BillingEntry, String>, ObservableValue<String>> propertyGetter;
        private final BiConsumer<BillingEntry, String> updateCallback;
        private TextField textField;

        public EditableCell(Function<TableColumn.CellDataFeatures<BillingEntry, String>, ObservableValue<String>> propertyGetter,
                            BiConsumer<BillingEntry, String> updateCallback) {
            this.propertyGetter = propertyGetter;
            this.updateCallback = updateCallback;
        }

        @Override
        public void startEdit() {
            if (!isEmpty()) {
                super.startEdit();
                createTextField();
                setText(null);
                setGraphic(textField);
                textField.selectAll();
                textField.requestFocus();
            }
        }

        @Override
        public void cancelEdit() {
            super.cancelEdit();
            setText(getItem());
            setGraphic(null);
        }

        @Override
        public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                if (isEditing()) {
                    if (textField != null) {
                        textField.setText(item);
                    }
                    setText(null);
                    setGraphic(textField);
                } else {
                    setText(item);
                    setGraphic(null);
                    // 为单元格添加双击事件，触发编辑
                    setOnMouseClicked(event -> {
                        if (event.getClickCount() == 2 && !isEmpty()) {
                            startEdit();
                        }
                    });
                }
            }
        }

        private void createTextField() {
            textField = new TextField(getString());
            textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
            textField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                if (!isNowFocused) {
                    String newValue = textField.getText();
                    try {
                        // 获取当前行的数据项
                        BillingEntry entry = (BillingEntry) getTableRow().getItem();
                        if (entry != null) {
                            // 直接调用回调函数
                            logger.info("执行字符串类型编辑回调: " + newValue);
                            updateCallback.accept(entry, newValue);
                        }
                        commitEdit(newValue);
                    } catch (Exception e) {
                        logger.severe("编辑提交失败: " + e.getMessage());
                        e.printStackTrace();
                        cancelEdit();
                    }
                    // 强制刷新UI
                    Platform.runLater(() -> {
                        cancelEdit();
                        getTableView().refresh();
                    });
                }
            });
        }

        private String getString() {
            return getItem() == null ? "" : getItem();
        }
    }

    // 价格单元格的特殊实现，带有数字验证
    private class EditablePriceCell extends TableCell<BillingEntry, Double> {
        private final Function<TableColumn.CellDataFeatures<BillingEntry, Double>, DoubleProperty> propertyGetter;
        private final BiConsumer<BillingEntry, Double> updateCallback;
        private TextField textField;

        public EditablePriceCell(Function<TableColumn.CellDataFeatures<BillingEntry, Double>, DoubleProperty> propertyGetter,
                                BiConsumer<BillingEntry, Double> updateCallback) {
            this.propertyGetter = propertyGetter;
            this.updateCallback = updateCallback;
        }

        @Override
        public void startEdit() {
            if (!isEmpty()) {
                super.startEdit();
                createTextField();
                setText(null);
                setGraphic(textField);
                textField.selectAll();
                textField.requestFocus();
            }
        }

        @Override
        public void cancelEdit() {
            super.cancelEdit();
            updateItem(getItem(), false);
        }

        @Override
        public void updateItem(Double item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                setText(null);
                setStyle("");
                setGraphic(null);
            } else {
                if (isEditing()) {
                    if (textField != null) {
                        textField.setText(String.valueOf(item));
                    }
                    setText(null);
                    setGraphic(textField);
                } else {
                    // 显示格式化价格
                    String formattedPrice = String.format("%s¥%.2f", item >= 0 ? "+" : "-", Math.abs(item));
                    setText(formattedPrice);

                    if (item >= 0) {
                        setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #F44336; -fx-font-weight: bold;");
                    }

                    setGraphic(null);
                    // 为单元格添加双击事件，触发编辑
                    setOnMouseClicked(event -> {
                        if (event.getClickCount() == 2 && !isEmpty()) {
                            startEdit();
                        }
                    });
                }
            }
        }

        private void createTextField() {
            textField = new TextField(getItem() == null ? "" : String.valueOf(getItem()));
            textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
            
            // 设置数字格式验证
            textField.textProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal.matches("-?\\d*\\.?\\d*")) {
                    textField.setText(oldVal);
                }
            });
            
            textField.setOnAction(e -> commitNumericEdit());
            textField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                if (!isNowFocused) {
                    commitNumericEdit();
                }
            });
        }
        
        private void commitNumericEdit() {
            try {
                double value = Double.parseDouble(textField.getText());
                // 获取当前行的数据项
                BillingEntry entry = (BillingEntry) getTableRow().getItem();
                if (entry != null) {
                    // 直接调用回调函数
                    logger.info("执行价格编辑回调: " + value);
                    updateCallback.accept(entry, value);
                }
                commitEdit(value);
                // 强制刷新UI
                Platform.runLater(() -> {
                    cancelEdit();
                    getTableView().refresh();
                });
            } catch (NumberFormatException e) {
                logger.severe("数字格式无效: " + e.getMessage());
                cancelEdit();
            }
        }
    }

    /**
     * 强制保存后端数据到文件（无引号格式）
     * 在更新、删除等操作后调用，确保后端数据持久化
     */
    private void forceSaveBackendData() {
        logger.info("强制保存后端数据到文件");
        
        if (billingService == null) {
            logger.warning("billingService为空，无法保存后端数据");
            return;
        }
        
        if (needTokenRefresh()) {
            logger.warning("Token需要刷新，无法保存后端数据");
            return;
        }
        
        try {
            ApiResponse response = billingService.saveData();
            if (response != null && response.isSuccess()) {
                logger.info("后端数据保存成功: " + response.getMessage());
            } else {
                String errorMsg = (response != null) ? response.getMessage() : "未知错误";
                logger.warning("后端数据保存失败: " + errorMsg);
                
                // 降级处理：如果常规保存失败，尝试手动编写CSV文件
                tryWriteManualCsvFile();
            }
        } catch (Exception e) {
            logger.severe("保存后端数据时发生异常: " + e.getMessage());
            e.printStackTrace();
            
            // 降级处理：如果API调用失败，尝试手动编写CSV文件
            tryWriteManualCsvFile();
        }
    }

    /**
     * 手动将当前数据写入CSV文件
     * 当API保存失败时使用的备选方案
     */
    private void tryWriteManualCsvFile() {
        logger.info("尝试手动写入CSV文件");
        
        try {
            // 提取当前表格中的所有数据
            StringBuilder csvContent = new StringBuilder();
            
            // 写入数据行
            for (BillingEntry entry : billingData) {
                // 格式化价格，确保没有科学计数法
                String priceStr = String.format("%.2f", entry.priceProperty().get());
                
                // 格式化日期，从YYYY-MM-DD转为YYYY/MM/DD
                String dateStr = entry.timeProperty().get().toString().replace("-", "/");
                
                // 格式化为CSV行 - 格式: 类别,产品,日期,时间,价格,备注
                String line = String.format("%s,%s,%s,%s,%s,%s",
                        entry.categoryProperty().get(),
                        entry.productProperty().get(),
                        dateStr,
                        entry.timeHourProperty().get(),
                        priceStr,
                        entry.remarkProperty().get());
                
                csvContent.append(line).append("\n");
            }
            
            // 直接写入后端期望的路径
            String backendFilePath = "D:\\software-75-main\\data\\billing\\billingEntries.csv";
            logger.info("尝试写入后端文件: " + backendFilePath);
            
            try {
                // 创建目录（如果不存在）
                java.io.File directory = new java.io.File("D:\\software-75-main\\data\\billing");
                if (!directory.exists()) {
                    boolean created = directory.mkdirs();
                    logger.info("创建目录结果: " + created);
                }
                
                // 写入文件
                java.io.File csvFile = new java.io.File(backendFilePath);
                java.io.FileWriter writer = new java.io.FileWriter(csvFile);
                writer.write(csvContent.toString());
                writer.flush();
                writer.close();
                
                logger.info("成功写入后端CSV文件: " + csvFile.getAbsolutePath());
                
            } catch (java.io.IOException e) {
                logger.severe("写入后端文件失败: " + e.getMessage());
                e.printStackTrace();
                
                // 尝试备用方案：写入用户当前目录
                try {
                    String currentDir = System.getProperty("user.dir");
                    java.io.File backupFile = new java.io.File(currentDir, "billingEntries_backup.csv");
                    java.io.FileWriter backupWriter = new java.io.FileWriter(backupFile);
                    backupWriter.write(csvContent.toString());
                    backupWriter.close();
                    
                    logger.info("已写入备份文件: " + backupFile.getAbsolutePath());
                    
                    // 提示用户手动复制文件
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setTitle("需要手动操作");
                        alert.setHeaderText("无法直接写入后端文件");
                        alert.setContentText("已创建备份文件: " + backupFile.getAbsolutePath() + 
                                           "\n请手动复制此文件到: " + backendFilePath);
                        alert.showAndWait();
                    });
                } catch (Exception e2) {
                    logger.severe("创建备份文件也失败: " + e2.getMessage());
                    e2.printStackTrace();
                }
            }
        } catch (Exception e) {
            logger.severe("手动写入CSV文件失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 修改updateBillingEntry方法
    private void updateBillingEntry(BillingEntry entry) {
        logger.info("执行更新账单条目");
        
        if (billingService == null) {
            logger.severe("billingService为空，无法更新到后端");
            showAlert("服务错误", "账单服务未初始化，请刷新页面后重试");
            return;
        }
        
        if (needTokenRefresh()) {
            logger.warning("Token需要刷新，尝试自动刷新");
            try {
                refreshToken();
                logger.info("Token已刷新，继续更新操作");
            } catch (Exception e) {
                logger.severe("Token刷新失败: " + e.getMessage());
                showAlert("认证错误", "您的令牌已过期，请刷新令牌后重试");
                return;
            }
        }
        
        try {
            // 先更新前端显示，确保UI响应
            Platform.runLater(() -> billingTable.refresh());
            
            String entryId = entry.getCachedEntryId();
            logger.info("原始记录ID: " + entryId);
            
            // 如果ID为空，尝试从后端获取或生成ID
            if (entryId == null || entryId.isEmpty()) {
                logger.info("记录ID为空，尝试获取或生成ID");
                
                try {
                    // 使用日期+产品+类别组合查询
                    logger.info("查询条件: 日期=" + entry.timeProperty().get() + ", 类别=" + entry.categoryProperty().get() + ", 产品=" + entry.productProperty().get());
                    List<com.example.loginapp.api.BillingEntry> backendEntries = 
                        billingService.getEntries(entry.timeProperty().get(), null, entry.categoryProperty().get(), entry.productProperty().get());
                    
                    int entryCount = (backendEntries != null) ? backendEntries.size() : 0;
                    logger.info("查询到 " + entryCount + " 条匹配记录");
                    
                    // 查找匹配的记录
                    if (backendEntries != null && !backendEntries.isEmpty()) {
                        for (com.example.loginapp.api.BillingEntry backendEntry : backendEntries) {
                            logger.info("后端记录: id=" + backendEntry.getEntryId() + 
                                       ", 类别=" + backendEntry.getCategory() + 
                                       ", 产品=" + backendEntry.getProduct() + 
                                       ", 日期=" + backendEntry.getDate());
                            
                            if (backendEntry.getCategory().equals(entry.categoryProperty().get()) &&
                                backendEntry.getProduct().equals(entry.productProperty().get()) &&
                                backendEntry.getDate().equals(entry.timeProperty().get())) {
                                
                                entryId = backendEntry.getEntryId();
                                logger.info("找到匹配的后端ID: " + entryId);
                                entry.setEntryId(entryId);
                                break;
                            }
                        }
                    } else {
                        logger.warning("后端没有找到匹配记录");
                    }
                } catch (Exception e) {
                    logger.severe("查询后端ID失败: " + e.getMessage());
                    e.printStackTrace();
                }
                
                // 如果仍然没有ID，生成一个基于记录特征的ID
                if (entryId == null || entryId.isEmpty()) {
                    String generatedId = String.format("%s_%s_%s", 
                        entry.timeProperty().get().toString(),
                        entry.categoryProperty().get(),
                        entry.productProperty().get());
                    logger.info("生成新ID: " + generatedId);
                    entryId = generatedId;
                    entry.setEntryId(entryId);
                }
            }
            
            // 确保entryId的格式正确，要求包含至少一个下划线
            if (!entryId.contains("_")) {
                String formattedId = String.format("%s_%s_%s", 
                    entry.timeProperty().get().toString(),
                    entry.categoryProperty().get(),
                    entry.productProperty().get());
                logger.info("重新格式化ID: " + entryId + " -> " + formattedId);
                entryId = formattedId;
                entry.setEntryId(entryId);
            }
            
            // 组装更新参数
            BigDecimal price = BigDecimal.valueOf(entry.priceProperty().get());
            LocalDate date = entry.timeProperty().get();
            String timeStr = entry.timeHourProperty().get();
            String category = entry.categoryProperty().get();
            String product = entry.productProperty().get();
            String remark = entry.remarkProperty().get();
            
            // 处理日期和时间
            LocalTime time = null;
            boolean backendUpdateSuccess = false;
            
            try {
                // 确保时间格式正确
                if (timeStr == null || timeStr.isEmpty()) {
                    logger.info("时间字符串为空，使用当前时间");
                    time = LocalTime.now();
                } else {
                    try {
                        time = LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm"));
                        logger.info("成功解析时间: " + time);
                    } catch (Exception e) {
                        logger.warning("时间格式解析失败: " + e.getMessage());
                        time = LocalTime.now();
                    }
                }
                
                // 标准化为HH:mm格式
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
                time = LocalTime.parse(time.format(formatter), formatter);
                
                // 打印所有调用参数值
                logger.info("调用updateEntry参数详情:");
                logger.info("- entryId: " + entryId);
                logger.info("- category: " + category);
                logger.info("- product: " + product);
                logger.info("- price: " + price);
                logger.info("- date: " + date + " (" + date.getClass().getName() + ")");
                logger.info("- time: " + time + " (" + time.getClass().getName() + ")");
                logger.info("- remark: " + remark);
                
                try {
                    // 尝试调用API更新记录
                    BillingEntryResponse response = billingService.updateEntry(
                        entryId,
                        category,
                        product,
                        price,
                        date,
                        time,
                        remark
                    );
                    
                    // 检查响应
                    if (response != null) {
                        logger.info("API响应: success=" + response.isSuccess() + ", message=" + response.getMessage());
                        if (response.getError() != null) {
                            logger.warning("API错误: code=" + response.getError().getCode() + 
                                          ", message=" + response.getError().getMessage());
                        }
                        
                        if (response.isSuccess()) {
                            logger.info("更新成功，更新前端时间显示");
                            entry.timeHourProperty().set(time.format(formatter));
                            backendUpdateSuccess = true;
                        } else {
                            logger.warning("更新失败: " + response.getMessage());
                        }
                    } else {
                        logger.severe("API无响应");
                    }
                    
                    // 无论API是否成功，都尝试直接写入文件
                    tryWriteManualCsvFile();
                    
                } catch (Exception apiEx) {
                    logger.severe("API调用异常: " + apiEx.getMessage());
                    apiEx.printStackTrace();
                    
                    // API调用失败，直接写入文件
                    tryWriteManualCsvFile();
                }
            } catch (Exception timeEx) {
                logger.severe("时间处理异常: " + timeEx.getMessage());
                timeEx.printStackTrace();
            }
            
        } catch (Exception e) {
            logger.severe("更新过程中发生未处理异常: " + e.getMessage());
            e.printStackTrace();
            
            // 确保前端UI仍然更新
            Platform.runLater(() -> billingTable.refresh());
        }
    }

    public static class BillingEntry {
        private final StringProperty category;
        private final StringProperty product;
        private final DoubleProperty price;
        private final ObjectProperty<LocalDate> time;
        private final StringProperty timeHour;
        private final StringProperty formattedTime;
        private final StringProperty remark;
        private String entryId; // 添加entryId字段

        public BillingEntry(String category, String product, double price, LocalDate time, String timeHour) {
            this.category = new SimpleStringProperty(category);
            this.product = new SimpleStringProperty(product);
            this.price = new SimpleDoubleProperty(price);
            this.time = new SimpleObjectProperty<>(time);
            this.timeHour = new SimpleStringProperty(timeHour);
            this.formattedTime = new SimpleStringProperty(
                time.getYear() + "." +
                String.format("%02d", time.getMonthValue()) + "." +
                String.format("%02d", time.getDayOfMonth()) + " " +
                timeHour
            );
            this.remark = new SimpleStringProperty(product); // Using product as remark for now
            this.entryId = ""; // 初始化为空字符串
        }

        public StringProperty categoryProperty() { return category; }
        public StringProperty productProperty() { return product; }
        public DoubleProperty priceProperty() { return price; }
        public ObjectProperty<LocalDate> timeProperty() { return time; }
        public StringProperty timeHourProperty() { return timeHour; }
        public StringProperty formattedTimeProperty() { return formattedTime; }
        public StringProperty remarkProperty() { return remark; }
        
        // 获取entryId
        public String getEntryId() {
            return entryId;
        }
        
        // 设置entryId
        public void setEntryId(String entryId) {
            this.entryId = entryId;
        }
        
        // 获取缓存的entryId - 与BillingViewController中调用的方法名一致
        public String getCachedEntryId() {
            return entryId;
        }
    }

    private void createCircleRefreshButton() {
        try {
            // 创建刷新图标容器
            StackPane circleButton = new StackPane();
            circleButton.setMaxSize(25, 25);
            circleButton.setMinSize(25, 25);
            circleButton.setPrefSize(25, 25);
            
            // 设置透明背景
            circleButton.setStyle("-fx-background-color: transparent;");
            
            // 刷新图标
            Label refreshIcon = new Label("⟳");
            refreshIcon.setStyle("-fx-font-size: 18px; -fx-text-fill: #4285F4; -fx-font-weight: bold;");
            circleButton.getChildren().add(refreshIcon);
            
            // 悬停效果
            circleButton.setOnMouseEntered(e -> 
                refreshIcon.setStyle("-fx-font-size: 18px; -fx-text-fill: #2a75f3; -fx-font-weight: bold;"));
            
            circleButton.setOnMouseExited(e -> 
                refreshIcon.setStyle("-fx-font-size: 18px; -fx-text-fill: #4285F4; -fx-font-weight: bold;"));
            
            // 点击效果
            circleButton.setOnMousePressed(e -> 
                refreshIcon.setStyle("-fx-font-size: 18px; -fx-text-fill: #1a65e3; -fx-font-weight: bold;"));
            
            circleButton.setOnMouseReleased(e -> {
                refreshIcon.setStyle("-fx-font-size: 18px; -fx-text-fill: #4285F4; -fx-font-weight: bold;");
                refreshToken(); // 改为刷新令牌而不是刷新账单
            });
            
            // 设置提示文本
            Tooltip tooltip = new Tooltip("刷新认证令牌");
            Tooltip.install(circleButton, tooltip);
            
            // 查找Billing Details标题
            Scene scene = addCsvButton.getScene();
            if (scene != null) {
                for (javafx.scene.Node node : scene.getRoot().lookupAll("Label")) {
                    if (node instanceof Label && "Billing Details".equals(((Label)node).getText())) {
                        logger.info("找到Billing Details标题");
                        Pane parent = (Pane)node.getParent();
                        if (parent != null) {
                            // 在标题旁边添加按钮
                            parent.getChildren().add(circleButton);
                            double titleWidth = ((Label)node).getWidth();
                            if (titleWidth == 0) {
                                titleWidth = 100; // 设置一个默认宽度
                            }
                            circleButton.setLayoutX(node.getLayoutX() + titleWidth + 5);
                            circleButton.setLayoutY(node.getLayoutY());
                            logger.info("成功添加刷新按钮在Billing Details旁边");
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.severe("创建刷新按钮失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void removeAllRefreshButtons() {
        try {
            Scene scene = addCsvButton.getScene();
            if (scene != null) {
                // 删除所有可能的刷新按钮
                
                // 1. 删除"Refresh Auth"文本按钮
                for (javafx.scene.Node node : scene.getRoot().lookupAll("Button")) {
                    if (node instanceof Button) {
                        Button btn = (Button) node;
                        if ("Refresh Auth".equals(btn.getText())) {
                            if (btn.getParent() instanceof Pane) {
                                ((Pane)btn.getParent()).getChildren().remove(btn);
                                logger.info("删除了Refresh Auth按钮");
                            }
                        }
                    }
                }
                
                // 2. 删除可能存在的圆形刷新按钮
                for (javafx.scene.Node node : scene.getRoot().lookupAll("StackPane")) {
                    if (node.getStyle() != null && node.getStyle().contains("-fx-background-color: transparent")) {
                        // 这可能是我们之前创建的圆形按钮
                        if (node.getParent() instanceof Pane) {
                            ((Pane)node.getParent()).getChildren().remove(node);
                            logger.info("删除了现有的圆形刷新按钮");
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.warning("删除刷新按钮失败: " + e.getMessage());
        }
    }

    /**
     * 清空billingEntries.csv文件
     */
    private void clearBillingEntriesFile() {
        logger.info("开始清空账单数据文件");
        
        try {
            // 账单文件路径
            String csvFilePath = "D:\\software-75-main\\data\\billing\\billingEntries.csv";
            java.io.File csvFile = new java.io.File(csvFilePath);
            
            if (!csvFile.exists()) {
                logger.warning("CSV文件不存在，无需清空: " + csvFilePath);
                return;
            }
            
            // 清空文件内容 (写入空字符串)
            java.io.FileWriter writer = new java.io.FileWriter(csvFile);
            writer.write("");
            writer.close();
            
            logger.info("账单数据文件已清空: " + csvFilePath);
            
            // 清空表格数据
            billingData.clear();
            billingTable.refresh();
        } catch (Exception e) {
            logger.severe("清空账单数据文件失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddCsv() {
        logger.info("开始CSV导入");
        
        // 在导入前检查token
        if (needTokenRefresh()) {
            logger.info("Token需要刷新，提示用户");
            showAlert("需要刷新", "您的令牌可能已过期，请点击刷新按钮或重新登录后再尝试");
            return;
        }
        
        // 创建文件选择器
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择CSV文件");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV文件", "*.csv")
        );
        
        // 设置初始目录为桌面
        try {
            fileChooser.setInitialDirectory(
                new File(System.getProperty("user.home") + "/Desktop")
            );
        } catch (Exception e) {
            logger.warning("无法设置初始目录到桌面: " + e.getMessage());
        }
        
        // 显示文件选择对话框
        Stage stage = (Stage) addCsvButton.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);
        
        if (selectedFile == null) {
            logger.info("用户取消了文件选择");
            return;
        }
        
        logger.info("用户选择的文件: " + selectedFile.getAbsolutePath());
        
        // 显示确认对话框
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("确认导入");
        confirmDialog.setHeaderText("确认导入CSV数据");
        confirmDialog.setContentText("您确定要导入文件: " + selectedFile.getName() + "?\n此操作可能会添加多条数据记录。");
        
        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                importCsvFile(selectedFile);
            }
        });
    }

    private void importCsvFile(File file) {
        logger.info("开始导入文件: " + file.getAbsolutePath());
        
        try {
            // 保存当前表格中的数据
            List<BillingEntry> currentData = new ArrayList<>(billingData);
            logger.info("保存当前表格数据，共 " + currentData.size() + " 条记录");
            
            // 先尝试直接读取并解析CSV文件
            List<BillingEntry> importedEntries = new ArrayList<>();
            int importedCount = 0;
            
            try {
                // 读取CSV文件内容
                List<String> lines = java.nio.file.Files.readAllLines(file.toPath(), java.nio.charset.StandardCharsets.UTF_8);
                logger.info("CSV文件共 " + lines.size() + " 行");
                
                if (!lines.isEmpty()) {
                    for (String line : lines) {
                        logger.info("处理CSV行: " + line);
                        
                        try {
                            // 解析CSV行 - 支持多种格式
                            String[] parts = line.split(",");
                            if (parts.length < 4) {
                                logger.warning("CSV行格式不正确: " + line);
                                continue;
                            }
                            
                            // 根据图片示例，格式为: 类别,产品,日期,时间,价格,备注
                            String category = parts[0].trim();
                            String product = parts[1].trim();
                            
                            // 解析日期 - 支持多种格式(YYYY/MM/DD 或 YYYY-MM-DD)
                            LocalDate date;
                            String dateStr = parts[2].trim();
                            try {
                                // 处理YYYY/MM/DD格式
                                if (dateStr.contains("/")) {
                                    String[] dateParts = dateStr.split("/");
                                    if (dateParts.length == 3) {
                                        int year = Integer.parseInt(dateParts[0]);
                                        int month = Integer.parseInt(dateParts[1]);
                                        int day = Integer.parseInt(dateParts[2]);
                                        date = LocalDate.of(year, month, day);
                                    } else {
                                        throw new Exception("无效的日期格式");
                                    }
                                } else {
                                    // 尝试标准格式 YYYY-MM-DD
                                    date = LocalDate.parse(dateStr);
                                }
                            } catch (Exception e) {
                                logger.warning("日期格式错误，使用当前日期: " + dateStr + ", 错误: " + e.getMessage());
                                date = LocalDate.now();
                            }
                            
                            // 解析时间
                            String timeStr = parts[3].trim();
                            if (timeStr.length() > 5) {
                                // 如果时间格式为HH:MM:SS，只保留HH:MM部分
                                timeStr = timeStr.substring(0, 5);
                            }
                            
                            // 解析价格
                            double price;
                            try {
                                // 假设价格在第5列
                                if (parts.length > 4) {
                                    price = Double.parseDouble(parts[4].trim());
                                } else {
                                    price = 0.0;
                                    logger.warning("价格字段缺失，默认为0");
                                }
                            } catch (NumberFormatException e) {
                                logger.warning("价格格式错误，默认为0: " + parts[4]);
                                price = 0.0;
                            }
                            
                            // 解析备注
                            String remark = parts.length > 5 ? parts[5].trim() : product;
                            
                            // 创建账单条目并添加到本地列表
                            BillingEntry entry = new BillingEntry(category, product, price, date, timeStr);
                            entry.remarkProperty().set(remark);
                            
                            importedEntries.add(entry);
                            importedCount++;
                            logger.info("成功解析条目: " + category + ", " + product + ", 价格=" + price + ", 日期=" + date + ", 时间=" + timeStr);
                            
                            // 调用API向后端添加条目
                            try {
                                // 先转换为API所需的对象
                                BigDecimal bdPrice = BigDecimal.valueOf(price);
                                
                                // 解析时间字符串
                                LocalTime time;
                                try {
                                    time = LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm"));
                                } catch (Exception e) {
                                    logger.warning("时间格式解析失败，使用默认时间: " + timeStr);
                                    time = LocalTime.of(12, 0); // 默认中午12点
                                }
                                
                                // 调用API创建条目
                                BillingEntryResponse response = billingService.createEntry(
                                    category, product, bdPrice, date, time, remark
                                );
                                
                                if (response != null && response.isSuccess()) {
                                    logger.info("成功创建条目: " + category + ", " + product);
                                    
                                    // 生成一个ID
                                    String entryId = String.format("%s_%s_%s", 
                                        date.toString(),
                                        category.replaceAll("[\\s:,]+", "_"),
                                        product.replaceAll("[\\s:,]+", "_"));
                                    entry.setEntryId(entryId);
                                } else {
                                    logger.warning("创建条目失败: " + (response != null ? response.getMessage() : "null response"));
                                }
                            } catch (Exception e) {
                                logger.severe("API调用失败: " + e.getMessage());
                                e.printStackTrace();
                            }
                        } catch (Exception e) {
                            logger.warning("解析CSV行失败: " + line + ", 错误: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }
                
                // 添加导入的条目到表格
                billingData.addAll(importedEntries);
                
                // 刷新表格显示
                billingTable.setItems(null);
                billingTable.setItems(billingData);
                billingTable.refresh();
                
                // 写入到后端文件
                tryWriteManualCsvFile();
                
                logger.info("CSV导入完成，共导入 " + importedCount + " 条记录");
                
                // 显示导入结果
                String message = String.format("导入成功：\n成功导入 %d 条记录", importedCount);
                Platform.runLater(() -> {
                    showInfo("导入完成", message);
                });
                
            } catch (Exception e) {
                logger.severe("读取CSV文件失败，尝试使用API导入: " + e.getMessage());
                e.printStackTrace();
                
                // 如果直接读取失败，尝试使用API导入
                try {
                    // 异步执行导入
                    CompletableFuture.supplyAsync(() -> {
                        try {
                            // 调用服务导入CSV
                            ImportResponse response = billingService.importFromCsv(file);
                            logger.info("API导入成功，导入 " + response.getEntriesImported() + " 条，跳过 " + response.getEntriesSkipped() + " 条");
                            return response;
                        } catch (ApiException e2) {
                            logger.severe("API导入CSV文件失败: " + e2.getMessage());
                            e2.printStackTrace();
                            throw new CompletionException(e2);
                        }
                    }).thenAccept(response -> {
                        // 在UI线程更新界面
                        Platform.runLater(() -> {
                            try {
                                // 显示导入结果
                                String message = String.format(
                                    "导入成功：\n成功导入 %d 条记录\n跳过 %d 条记录", 
                                    response.getEntriesImported(), 
                                    response.getEntriesSkipped()
                                );
                                
                                showInfo("导入完成", message);
                                
                                // 直接从CSV文件加载数据
                                billingData.clear();
                                tryReadFromCsvFile();
                            } catch (Exception e2) {
                                logger.severe("导入后加载数据时发生错误: " + e2.getMessage());
                                e2.printStackTrace();
                            }
                        });
                    }).exceptionally(ex -> {
                        logger.severe("导入过程发生异常: " + ex.getMessage());
                        ex.printStackTrace();
                        
                        Platform.runLater(() -> {
                            showAlert("导入失败", "导入CSV文件时发生错误: " + ex.getMessage());
                        });
                        
                        return null;
                    });
                } catch (Exception apiE) {
                    logger.severe("启动API导入过程失败: " + apiE.getMessage());
                    showAlert("导入失败", "无法启动导入过程: " + apiE.getMessage());
                }
            }
        } catch (Exception e) {
            logger.severe("导入过程发生未处理异常: " + e.getMessage());
            showAlert("导入失败", "导入过程发生错误: " + e.getMessage());
        }
    }
}