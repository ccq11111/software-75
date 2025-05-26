package com.example.software.view;

import com.example.software.api.ApiException;
import com.example.software.api.ApiServiceFactory;
import com.example.software.api.MockSavingsService;
import com.example.software.api.SavingsService;
import com.example.software.model.SavingPlanModel;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.util.List;

/**
 * Controller for the Set View
 */
public class SetViewController {
    // UI Components
    @FXML private Label usernameLabel;
    @FXML private Button addPlanButton;
    @FXML private TableView<SavingPlanModel.SavingPlan> planTable;
    @FXML private TableColumn<SavingPlanModel.SavingPlan, String> nameColumn;
    @FXML private TableColumn<SavingPlanModel.SavingPlan, String> startDateColumn;
    @FXML private TableColumn<SavingPlanModel.SavingPlan, String> savedColumn;
    @FXML private TableColumn<SavingPlanModel.SavingPlan, String> cycleTimeColumn;
    @FXML private TableColumn<SavingPlanModel.SavingPlan, String> aimMoneyColumn;
    @FXML private TableColumn<SavingPlanModel.SavingPlan, SavingPlanModel.SavingPlan> actionColumn;
    @FXML private PieChart assetsChart;
    @FXML private Label totalAssetsLabel;
    @FXML private Button backupButton;
    @FXML private Button logoutButton;

    // Get data from the shared model
    private ObservableList<SavingPlanModel.SavingPlan> planItems = SavingPlanModel.getInstance().getPlans();

    // Savings service for API operations
    private SavingsService savingsService;

    @FXML
    public void initialize() {
        try {
            // Set up username (will be set by the calling controller)
            if (usernameLabel != null) {
                usernameLabel.setText("Username");
            }

            // 初始化API服务 - 使用ApiServiceFactory获取服务
            try {
                savingsService = ApiServiceFactory.getInstance().getSavingsService();
                System.out.println("成功初始化SavingsService: " + savingsService.getClass().getName());
            } catch (Exception e) {
                System.err.println("无法获取SavingsService，将使用MockSavingsService: " + e.getMessage());
                savingsService = new MockSavingsService();
            }

            // 从服务加载计划数据
            loadPlansFromService();

            // Initialize table
            initializeTable();

            // Initialize chart
            initializeChart();

            // 设置自动刷新计划数据的定时器
            java.util.Timer refreshTimer = new java.util.Timer(true);
            refreshTimer.schedule(new java.util.TimerTask() {
                @Override
                public void run() {
                    try {
                        // 在JavaFX线程上执行UI更新
                        javafx.application.Platform.runLater(() -> {
                            try {
                                System.out.println("执行定时刷新...");
                                loadPlansFromService();
                                planTable.refresh();
                                initializeChart();
                            } catch (Exception e) {
                                System.err.println("定时刷新失败: " + e.getMessage());
                            }
                        });
                    } catch (Exception e) {
                        System.err.println("刷新计划数据失败: " + e.getMessage());
                    }
                }
            }, 5000, 30000); // 5秒后开始，每30秒刷新一次

            // Set up button handlers
            if (addPlanButton != null) {
                addPlanButton.setOnAction(event -> {
                    handleAddPlan();
                    // 返回时刷新计划列表
                    loadPlansFromService();
                    initializeChart();
                });
            }

            if (backupButton != null) {
                backupButton.setOnAction(event -> handleBackup());
            }

            if (logoutButton != null) {
                logoutButton.setOnAction(event -> handleLogout());
            }
        } catch (Exception e) {
            System.err.println("Error initializing Set view: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 从savings服务加载计划数据到SavingPlanModel
     */
    private void loadPlansFromService() {
        try {
            // 清空现有的计划列表
            planItems.clear();
            SavingPlanModel.getInstance().getPlans().clear();
            System.out.println("已清空现有计划列表");

            // 从服务获取所有计划
            System.out.println("开始从API获取计划...");
            List<com.example.software.api.SavingPlan> apiPlans = savingsService.getAllPlans();
            System.out.println("从API获取到 " + apiPlans.size() + " 个计划");

            if (apiPlans.isEmpty()) {
                System.out.println("API返回的计划列表为空");
                return;
            }

            // 将API计划转换为UI模型计划并添加到列表
            for (com.example.software.api.SavingPlan apiPlan : apiPlans) {
                System.out.println("处理计划: ID=" + apiPlan.getPlanId() +
                        ", 名称=" + apiPlan.getName() +
                        ", 日期=" + apiPlan.getStartDateConverted() +
                        ", 已存金额=" + apiPlan.getSavedAmount());

                SavingPlanModel.SavingPlan uiPlan = new SavingPlanModel.SavingPlan(
                        apiPlan.getName(),
                        apiPlan.getStartDateConverted(),
                        apiPlan.getCycle(),
                        apiPlan.getCycleTimes(),
                        apiPlan.getAmount().doubleValue(),
                        apiPlan.getCurrency() // 使用API中的货币，如需格式化可以加上
                );
                // 设置planId和已保存金额
                uiPlan.setPlanId(apiPlan.getPlanId());
                uiPlan.setSavedAmount(apiPlan.getSavedAmount().doubleValue());

                // 添加到模型中
                SavingPlanModel.getInstance().addPlan(uiPlan);
                System.out.println("已添加计划到UI模型: " + uiPlan.getName() + ", ID=" + uiPlan.getPlanId());
            }

            // 刷新表格
            planTable.refresh();

            System.out.println("成功从服务加载并添加到UI模型 " + apiPlans.size() + " 个计划");
            System.out.println("UI模型中现有 " + planItems.size() + " 个计划");
        } catch (ApiException e) {
            System.err.println("从服务加载计划时出错: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "加载错误", "无法从服务加载储蓄计划: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("加载计划时发生未知错误: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "未知错误", "加载计划时发生未知错误: " + e.getMessage());
        }
    }

    /**
     * Initialize the plan table
     */
    private void initializeTable() {
        try {
            System.out.println("初始化储蓄计划表格...");

            // Configure name column
            nameColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getName()));

            // Configure start date column
            startDateColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getFormattedStartDate()));

            // Configure cycle time column
            cycleTimeColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getCycleTimeDisplay()));

            // Configure aim money column
            aimMoneyColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getAimMoneyDisplay()));

            // Configure saved column
            savedColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(
                            cellData.getValue().getCurrency().startsWith("CNY") ?
                                    String.format("¥%.0f", cellData.getValue().getAmount()) :
                                    cellData.getValue().getCurrency().substring(0, 3) + String.format("%.0f", cellData.getValue().getAmount())
                    ));

            // Configure action column with edit and delete buttons
            actionColumn.setCellFactory(param -> new TableCell<SavingPlanModel.SavingPlan, SavingPlanModel.SavingPlan>() {
                private final Button editButton = new Button("Edit");
                private final Button deleteButton = new Button("Delete");
                private final HBox buttonBox = new HBox(5, editButton, deleteButton);

                {
                    // 设置按钮样式
                    editButton.getStyleClass().add("action-button");
                    deleteButton.getStyleClass().add("action-button");
                    deleteButton.getStyleClass().add("delete-button");

                    // 设置编辑按钮事件
                    editButton.setOnAction(event -> {
                        SavingPlanModel.SavingPlan plan = getTableView().getItems().get(getIndex());
                        showEditDialog(plan);
                    });

                    // 设置删除按钮事件
                    deleteButton.setOnAction(event -> {
                        SavingPlanModel.SavingPlan plan = getTableView().getItems().get(getIndex());
                        handleDeletePlan(plan);
                    });
                }

                @Override
                protected void updateItem(SavingPlanModel.SavingPlan plan, boolean empty) {
                    super.updateItem(plan, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        setGraphic(buttonBox);
                    }
                }
            });

            // Set table items
            planTable.setItems(planItems);

            // Add placeholder message
            planTable.setPlaceholder(new Label("表中无内容，请添加储蓄计划"));

            System.out.println("表格初始化完成，当前有 " + planItems.size() + " 个计划");
        } catch (Exception e) {
            System.err.println("初始化表格时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Show dialog to edit a plan
     */
    private void showEditDialog(SavingPlanModel.SavingPlan plan) {
        try {
            // Check if plan ID is null
            if (plan.getPlanId() == null || plan.getPlanId().isEmpty()) {
                System.err.println("警告: 计划ID为空，无法编辑");
                showAlert(Alert.AlertType.WARNING, "编辑错误", "计划ID不存在，无法编辑");
                return;
            }

            System.out.println("正在编辑计划: ID=" + plan.getPlanId() + ", 名称=" + plan.getName());

            // Create the dialog
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("编辑储蓄计划");
            dialog.setHeaderText("修改 \"" + plan.getName() + "\" 计划");

            // Add buttons
            ButtonType saveButtonType = new ButtonType("保存", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

            // Create the form layout
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            // Create form fields
            TextField nameField = new TextField(plan.getName());
            TextField amountField = new TextField(String.valueOf(plan.getAmount()));
            ComboBox<String> cycleComboBox = new ComboBox<>();
            cycleComboBox.getItems().addAll("Daily", "Weekly", "Monthly", "Quarterly", "Yearly");
            cycleComboBox.setValue(plan.getCycle());

            // 添加已存金额字段
            TextField savedAmountField = new TextField(String.valueOf(plan.getSavedAmount()));

            // Add fields to grid
            grid.add(new Label("名称:"), 0, 0);
            grid.add(nameField, 1, 0);
            grid.add(new Label("金额:"), 0, 1);
            grid.add(amountField, 1, 1);
            grid.add(new Label("周期:"), 0, 2);
            grid.add(cycleComboBox, 1, 2);
            grid.add(new Label("已存金额:"), 0, 3);
            grid.add(savedAmountField, 1, 3);

            // Set the grid in the dialog
            dialog.getDialogPane().setContent(grid);

            // Request focus on the name field
            nameField.requestFocus();

            // Show the dialog and process the result
            dialog.showAndWait().ifPresent(result -> {
                if (result == saveButtonType) {
                    try {
                        // Get the new values
                        String newName = nameField.getText();
                        double newAmount = Double.parseDouble(amountField.getText());
                        String newCycle = cycleComboBox.getValue();
                        double newSavedAmount = Double.parseDouble(savedAmountField.getText());

                        // 使用计划的实际ID
                        String planId = plan.getPlanId();
                        System.out.println("保存更改: 计划ID=" + planId + ", 新名称=" + newName + ", 新已存金额=" + newSavedAmount);

                        // 尝试通过API更新计划
                        try {
                            // 转换为相应的API数据类型
                            BigDecimal amount = new BigDecimal(newAmount);
                            BigDecimal savedAmount = new BigDecimal(newSavedAmount);

                            // 确保startDate使用正确的格式，以避免403错误
                            String dateStr = plan.getStartDate().atStartOfDay().toInstant(ZoneOffset.UTC).toString();
                            System.out.println("发送更新请求: planId=" + planId + ", startDate=" + dateStr);

                            savingsService.updatePlan(
                                    planId,
                                    newName,
                                    plan.getStartDate(),
                                    newCycle,
                                    plan.getCycleTimes(),
                                    amount,
                                    plan.getCurrency().startsWith("CNY") ? "CNY" :
                                            plan.getCurrency().startsWith("USD") ? "USD" : "CNY", // 确保使用有效的货币代码
                                    savedAmount
                            );

                            // API更新成功，更新UI
                            plan.setName(newName);
                            plan.setAmount(newAmount);
                            plan.setCycle(newCycle);
                            plan.setSavedAmount(newSavedAmount);

                            // 重新加载计划列表
                            loadPlansFromService();

                            // Update the table
                            planTable.refresh();

                            // Update the chart
                            initializeChart();

                            // Show success message
                            showAlert(Alert.AlertType.INFORMATION, "计划已更新", "计划 \"" + newName + "\" 已成功更新。");
                        } catch (ApiException e) {
                            System.err.println("API更新计划失败: " + e.getMessage());
                            e.printStackTrace();

                            // API调用失败，仍然更新UI
                            plan.setName(newName);
                            plan.setAmount(newAmount);
                            plan.setCycle(newCycle);
                            plan.setSavedAmount(newSavedAmount);

                            planTable.refresh();
                            initializeChart();

                            System.err.println("API更新计划失败，但已更新UI: " + e.getMessage());
                            showAlert(Alert.AlertType.INFORMATION, "计划已更新", "计划 \"" + newName + "\" 已在本地更新。");
                        }
                    } catch (NumberFormatException e) {
                        showAlert(Alert.AlertType.ERROR, "输入错误", "请输入有效的金额数值。");
                    }
                }
            });
        } catch (Exception e) {
            System.err.println("Error showing edit dialog: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "错误", "无法打开编辑对话框: " + e.getMessage());
        }
    }

    /**
     * Handle deleting a plan
     */
    private void handleDeletePlan(SavingPlanModel.SavingPlan plan) {
        try {
            // Check if plan ID is null
            if (plan.getPlanId() == null || plan.getPlanId().isEmpty()) {
                System.err.println("警告: 计划ID为空，无法删除");
                showAlert(Alert.AlertType.WARNING, "删除错误", "计划ID不存在，无法删除");
                return;
            }

            System.out.println("准备删除计划: ID=" + plan.getPlanId() + ", 名称=" + plan.getName());

            // Ask for confirmation
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("确认删除");
            confirmAlert.setHeaderText("删除计划 \"" + plan.getName() + "\"");
            confirmAlert.setContentText("您确定要删除此计划吗？此操作无法撤销。");

            confirmAlert.showAndWait().ifPresent(result -> {
                if (result == ButtonType.OK) {
                    try {
                        // 获取实际的计划ID
                        String planId = plan.getPlanId();
                        System.out.println("确认删除计划: ID=" + planId);

                        // 尝试通过API删除计划
                        try {
                            savingsService.deletePlan(planId);
                            // API删除成功，从UI移除
                            planItems.remove(plan);

                            // 重新加载计划列表
                            loadPlansFromService();

                            // Update the chart
                            initializeChart();

                            // Show success message
                            showAlert(Alert.AlertType.INFORMATION, "计划已删除", "计划 \"" + plan.getName() + "\" 已成功删除。");
                        } catch (ApiException e) {
                            System.err.println("API删除计划失败: " + e.getMessage());
                            e.printStackTrace();

                            // API调用失败，仍然从UI移除
                            planItems.remove(plan);

                            // 尝试重新加载计划列表
                            try {
                                loadPlansFromService();
                            } catch (Exception ex) {
                                System.err.println("重新加载计划列表失败: " + ex.getMessage());
                            }

                            initializeChart();

                            System.err.println("API删除计划失败，但已从UI移除: " + e.getMessage());
                            // 仍然显示成功消息
                            showAlert(Alert.AlertType.INFORMATION, "计划已删除", "计划 \"" + plan.getName() + "\" 已从本地删除。");
                        }
                    } catch (Exception e) {
                        System.err.println("Error deleting plan: " + e.getMessage());
                        e.printStackTrace();
                        showAlert(Alert.AlertType.ERROR, "删除错误", "删除计划时出错: " + e.getMessage());
                    }
                }
            });
        } catch (Exception e) {
            System.err.println("Error showing delete confirmation: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "错误", "无法显示删除确认对话框: " + e.getMessage());
        }
    }

    /**
     * Show an alert dialog
     */
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Initialize the assets chart
     */
    private void initializeChart() {
        // Calculate total assets from all plans
        double totalAssets = calculateTotalAssets();

        // Create chart data
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("Assets", totalAssets)
        );

        assetsChart.setData(pieChartData);
        assetsChart.setLegendVisible(false);

        // Apply custom colors to the chart
        pieChartData.get(0).getNode().setStyle("-fx-pie-color: #ff6b6b;");

        // Make the chart a donut chart
        assetsChart.setStartAngle(90);

        // Set total assets label
        if (totalAssetsLabel != null) {
            totalAssetsLabel.setText(String.format("¥%.2f", totalAssets));
        }
    }

    /**
     * Calculate total assets from all plans
     */
    private double calculateTotalAssets() {
        double total = 0.0;

        System.out.println("计算总资产，计划数量: " + planItems.size());

        // Sum up all plan amounts
        for (SavingPlanModel.SavingPlan plan : planItems) {
            System.out.println("计划: " + plan.getName() + ", 货币: " + plan.getCurrency() +
                    ", 单次金额: " + plan.getAmount() +
                    ", 周期次数: " + plan.getCycleTimes() +
                    ", 总计划金额: " + plan.calculateTotalAmount());

            // 计算计划总金额（单次金额 × 周期次数），而非已储存金额
            if (plan.getCurrency().startsWith("CNY")) {
                double planTotal = plan.calculateTotalAmount();
                total += planTotal;
                System.out.println("添加到总额: " + planTotal + ", 当前总额: " + total);
            }
        }

        System.out.println("最终总资产: " + total);
        return total;
    }

    /**
     * Handle add plan button click - Navigate to Saving view
     */
    private void handleAddPlan() {
        try {
            // Get the parent BaseViewController to navigate to the Saving view
            Node node = addPlanButton.getScene().getRoot();
            if (node instanceof Parent) {
                Parent parent = (Parent) node;
                BaseViewController baseController = null;

                // Find the BaseViewController
                for (Node child : parent.getChildrenUnmodifiable()) {
                    if (child.getId() != null && child.getId().equals("contentArea")) {
                        baseController = (BaseViewController) parent.getUserData();
                        break;
                    }
                }

                // If we found the controller, use it to navigate
                if (baseController != null) {
                    // Call the savingButton's action event
                    baseController.navigateToSaving();
                } else {
                    // Fallback: manually load the Saving view
                    navigateToSavingManually();
                }
            } else {
                // Fallback: manually load the Saving view
                navigateToSavingManually();
            }
        } catch (Exception e) {
            System.err.println("Error handling add plan: " + e.getMessage());
            e.printStackTrace();

            // Fallback: show an alert
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Navigation Error");
            alert.setHeaderText(null);
            alert.setContentText("Could not navigate to the Saving view. Please try again.");
            alert.showAndWait();
        }
    }

    /**
     * Fallback method to manually navigate to the Saving view
     */
    private void navigateToSavingManually() {
        try {
            // Get the current stage
            Stage stage = (Stage) addPlanButton.getScene().getWindow();

            // Load the base view again
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/BaseView.fxml"));
            Parent baseView = loader.load();

            // Get the controller and set the username
            BaseViewController baseViewController = loader.getController();
            baseViewController.setUsername(username);

            // Get current window size for the new scene
            double width = stage.getWidth();
            double height = stage.getHeight();

            // Set the scene
            Scene scene = new Scene(baseView, width, height);
            stage.setScene(scene);

            // Navigate to the saving view
            baseViewController.navigateToSaving();
        } catch (IOException e) {
            System.err.println("Error navigating to Saving view: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle backup button click
     */
    private void handleBackup() {
        try {
            // In a real app, this would trigger a backup process
            // For now, just show an alert
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Backup");
            alert.setHeaderText(null);
            alert.setContentText("Backup functionality would be implemented here.");
            alert.showAndWait();
        } catch (Exception e) {
            System.err.println("Error handling backup: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle logout button click - Navigate to Login view
     */
    private void handleLogout() {
        try {
            // Get the current stage
            Stage stage = (Stage) logoutButton.getScene().getWindow();

            // Load the login view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
            Parent loginView = loader.load();

            // Create the new scene
            Scene scene = new Scene(loginView, 1024, 768);
            stage.setScene(scene);
            stage.setTitle("Login");

        } catch (IOException e) {
            System.err.println("Error navigating to Login view: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Username property
    private String username;

    /**
     * Set the username
     */
    public void setUsername(String username) {
        this.username = username;
        if (usernameLabel != null) {
            usernameLabel.setText(username);
        }
    }

    /**
     * Get the username
     */
    public String getUsername() {
        return username;
    }
}
