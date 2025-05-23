package com.example.software.view;

import com.example.software.api.*;
import com.example.software.model.SavingPlanModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Locale;

/**
 * Controller for the Saving View
 */
public class SavingViewController {
    @FXML private Label usernameLabel; // Only needed for displaying username

    // Form fields
    @FXML private TextField planNameField;
    @FXML private DatePicker startDatePicker;
    @FXML private ComboBox<String> cycleComboBox;
    @FXML private ComboBox<Integer> cycleTimesComboBox;
    @FXML private Slider amountSlider;
    @FXML private Label amountLabel;
    @FXML private ComboBox<String> currencyComboBox;
    @FXML private Button savePlanButton;

    // API service factory
    private final ApiServiceFactory apiServiceFactory = ApiServiceFactory.getInstance();

    // Currency formatter
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.CHINA);

    @FXML
    public void initialize() {
        try {
            // Set up username (will be set by the calling controller)
            if (usernameLabel != null) {
                usernameLabel.setText("Username");
            }

            // Initialize form fields
            initializeFormFields();

            // We no longer need menu button handlers as they are handled by BaseViewController

            // Set up slider listener with smoother updates
            if (amountSlider != null && amountLabel != null) {
                amountSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
                    // Format the value as currency
                    String formattedValue = currencyFormat.format(newValue.doubleValue());
                    // Update the UI with the formatted value
                    amountLabel.setText(formattedValue);

                    // Add visual feedback by changing the label color when the value changes significantly
                    if (Math.abs(newValue.doubleValue() - oldValue.doubleValue()) > 500) {
                        amountLabel.setStyle("-fx-text-fill: #ffe100; -fx-font-weight: bold;");
                        // Reset the style after a short delay
                        new java.util.Timer().schedule(
                            new java.util.TimerTask() {
                                @Override
                                public void run() {
                                    javafx.application.Platform.runLater(() ->
                                        amountLabel.setStyle("-fx-text-fill: #5e3c1c; -fx-font-weight: bold;"));
                                }
                            }, 300
                        );
                    }
                });

                // Initialize slider value
                String initialValue = currencyFormat.format(amountSlider.getValue());
                amountLabel.setText(initialValue);
            }

            // Set up save plan button handler
            if (savePlanButton != null) {
                savePlanButton.setOnAction(event -> handleSavePlan());
            }
            
            // 每次进入页面时自动显示AI储蓄计划提示
            // 稍微延迟加载，确保UI先渲染完成
            new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        javafx.application.Platform.runLater(() -> showSavingPlansAIPrompt());
                    }
                }, 500
            );
        } catch (Exception e) {
            System.err.println("Error initializing SavingViewController: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Initialize form fields with default values
     */
    private void initializeFormFields() {
        try {
            // Set current date as default for date picker
            if (startDatePicker != null) {
                startDatePicker.setValue(LocalDate.now());
            }

            // Initialize cycle combo box
            if (cycleComboBox != null) {
                ObservableList<String> cycleOptions = FXCollections.observableArrayList(
                        "Daily", "Weekly", "Monthly", "Quarterly", "Yearly"
                );
                cycleComboBox.setItems(cycleOptions);
            }

            // Initialize cycle times combo box
            if (cycleTimesComboBox != null) {
                ObservableList<Integer> cycleTimesOptions = FXCollections.observableArrayList();
                for (int i = 1; i <= 60; i++) {
                    cycleTimesOptions.add(i);
                }
                cycleTimesComboBox.setItems(cycleTimesOptions);
            }

            // Initialize currency combo box - 仅使用后端支持的货币类型
            if (currencyComboBox != null) {
                ObservableList<String> currencyOptions = FXCollections.observableArrayList(
                        "CNY (¥)", "USD ($)", "EUR (€)"
                );
                currencyComboBox.setItems(currencyOptions);
                currencyComboBox.setValue("CNY (¥)");
            }
        } catch (Exception e) {
            System.err.println("Error initializing form fields: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Navigation methods removed as they are now handled by BaseViewController

    /**
     * Handle saving a new plan
     */
    private void handleSavePlan() {
        try {
            // Check if UI components are available
            if (planNameField == null || cycleComboBox == null || cycleTimesComboBox == null ||
                startDatePicker == null || amountSlider == null || currencyComboBox == null) {
                System.err.println("Cannot save plan: UI components are not initialized");
                return;
            }

            // Validate inputs
            if (planNameField.getText().trim().isEmpty()) {
                showAlert("Error", "Please enter a plan name");
                return;
            }

            if (cycleComboBox.getValue() == null) {
                showAlert("Error", "Please select a cycle");
                return;
            }

            if (cycleTimesComboBox.getValue() == null) {
                showAlert("Error", "Please select cycle times");
                return;
            }

            if (currencyComboBox.getValue() == null) {
                showAlert("Error", "Please select a currency");
                return;
            }

            // Get values from form
            String planName = planNameField.getText().trim();
            LocalDate startDate = startDatePicker.getValue();
            String cycle = cycleComboBox.getValue();
            int cycleTimes = cycleTimesComboBox.getValue();
            BigDecimal amount = BigDecimal.valueOf(amountSlider.getValue());
            String currency = extractCurrencyCode(currencyComboBox.getValue());
            
            // 确保货币类型为后端支持的类型
            if (!currency.equals("CNY") && !currency.equals("USD") && !currency.equals("EUR")) {
                System.out.println("不支持的货币类型 " + currency + "，将默认使用CNY");
                currency = "CNY";
            }
            
            System.out.println("准备创建计划: 名称=" + planName + ", 开始日期=" + startDate + 
                ", 周期=" + cycle + ", 周期次数=" + cycleTimes + 
                ", 金额=" + amount + ", 货币=" + currency);

            try {
                // Get the savings service
                SavingsService savingsService = apiServiceFactory.getSavingsService();
                System.out.println("获取到SavingsService: " + savingsService.getClass().getName());

                // Create the plan
                System.out.println("调用createPlan API");
                try {
                    SavingPlanResponse response = savingsService.createPlan(
                        planName, startDate, cycle, cycleTimes, amount, currency);
                    
                    if (response == null) {
                        System.err.println("API返回的响应为null");
                        showAlert("Error", "创建计划失败: API返回空响应");
                        return;
                    }
                    
                    System.out.println("API创建计划成功，ID=" + response.getPlanId());
                    
                    // Also add to local model for display in other views
                    SavingPlanModel.SavingPlan localPlan = new SavingPlanModel.SavingPlan(
                        planName, startDate, cycle, cycleTimes, amount.doubleValue(), currency);
                    
                    // 设置从API返回的planId
                    if (response.getPlanId() != null && !response.getPlanId().isEmpty()) {
                        localPlan.setPlanId(response.getPlanId());
                        System.out.println("设置本地计划ID=" + response.getPlanId());
                    } else {
                        System.err.println("警告: API返回的planId为空");
                    }
                    
                    SavingPlanModel.getInstance().addPlan(localPlan);
                    System.out.println("计划已添加到本地模型");
                    
                    // Show a success message with calculated values
                    String message = String.format(
                            "Plan '%s' created successfully!\n\n" +
                            "Start Date: %s\n" +
                            "End Date: %s\n" +
                            "Cycle: %s\n" +
                            "Cycle Times: %d\n" +
                            "Amount per cycle: %s\n" +
                            "Total amount: %s\n" +
                            "Currency: %s",
                            response.getName(),
                            response.getStartDateAsLocalDate(),
                            response.getEndDateAsLocalDate(),
                            response.getCycle(),
                            response.getCycleTimes(),
                            currencyFormat.format(response.getAmount()),
                            currencyFormat.format(response.getTotalAmount()),
                            response.getCurrency());
    
                    showInfo("Plan Created", message);
    
                    // Clear form for next entry
                    planNameField.clear();
                    amountSlider.setValue(0);
                } catch (ApiException e) {
                    System.err.println("API Error saving plan: " + e.getMessage());
                    e.printStackTrace();
                    
                    // 尝试添加到本地模型，即使API调用失败
                    try {
                        System.out.println("尝试在API失败的情况下添加本地计划");
                        
                        SavingPlanModel.SavingPlan localPlan = new SavingPlanModel.SavingPlan(
                            planName, startDate, cycle, cycleTimes, amount.doubleValue(), currency);
                        
                        // 生成一个临时ID
                        String tempId = "temp-" + java.util.UUID.randomUUID().toString();
                        localPlan.setPlanId(tempId);
                        
                        SavingPlanModel.getInstance().addPlan(localPlan);
                        System.out.println("临时计划已添加到本地模型，ID=" + tempId);
                        
                        showAlert("Warning", "计划已在本地创建，但未能保存到服务器: " + e.getMessage());
                    } catch (Exception ex) {
                        System.err.println("创建本地计划时出错: " + ex.getMessage());
                        showAlert("Error", "Failed to save plan: " + e.getMessage());
                    }
                }
            } catch (Exception e) {
                System.err.println("Error saving plan: " + e.getMessage());
                e.printStackTrace();
                showAlert("Error", "An error occurred while saving the plan");
            }
        } catch (Exception e) {
            System.err.println("Error saving plan: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "An error occurred while saving the plan");
        }
    }

    /**
     * Extract the currency code from the display string
     */
    private String extractCurrencyCode(String currencyDisplay) {
        if (currencyDisplay == null) {
            return "CNY";
        }

        if (currencyDisplay.startsWith("CNY")) {
            return "CNY";
        } else if (currencyDisplay.startsWith("USD")) {
            return "USD";
        } else if (currencyDisplay.startsWith("EUR")) {
            return "EUR";
        } else {
            return "CNY"; // 默认使用CNY
        }
    }

    /**
     * Show an information dialog
     */
    private void showInfo(String title, String message) {
        try {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        } catch (Exception e) {
            System.err.println("Error showing info dialog: " + e.getMessage());
            e.printStackTrace();
        }
    }

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
     * Show an alert dialog
     */
    private void showAlert(String title, String message) {
        try {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        } catch (Exception e) {
            System.err.println("Error showing alert dialog: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 显示储蓄计划AI提示
     * 自动读取所有储蓄计划并生成信息
     */
    private void showSavingPlansAIPrompt() {
        try {
            SavingsService savingsService = apiServiceFactory.getSavingsService();
            java.util.List<SavingPlan> plans = savingsService.getAllPlans();
            if (plans == null || plans.isEmpty()) {
                showInfo("You don't have any savings plans yet","Come and set a savings goal !");
                return;
            }
            
            StringBuilder sb = new StringBuilder();
            sb.append("Your savings plan:\n\n");
            
            for (SavingPlan plan : plans) {
                String name = plan.getName();
                String currency = getCurrencySymbol(plan.getCurrency());
                BigDecimal totalAmount = plan.getTotalAmount();
                if (totalAmount == null) {
                    totalAmount = plan.getAmount().multiply(new BigDecimal(plan.getCycleTimes()));
                }
                
                BigDecimal savedAmount = plan.getSavedAmount();
                if (savedAmount == null) {
                    savedAmount = BigDecimal.ZERO;
                }
                
                BigDecimal leftAmount = totalAmount.subtract(savedAmount);
                
                LocalDate endDate = plan.getEndDateConverted();
                long daysLeft = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), endDate);
                
                sb.append(String.format("Your [%s] plan goal is to save %s%.2F. You have saved %s%.2F, and there are still %s%.2F left to save. You are still %d days away from the goal. \n\n", 
                    name, 
                    currency, totalAmount.doubleValue(), 
                    currency, savedAmount.doubleValue(),
                    currency, leftAmount.doubleValue(), 
                    daysLeft));
            }
            
            showAIPromptDialog("AI Assistant remind", sb.toString());
        } catch (Exception e) {
            System.err.println("Error occurred when reading the savings plan: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 根据货币代码获取符号
     */
    private String getCurrencySymbol(String currencyCode) {
        if (currencyCode == null) {
            return "¥";
        }
        
        switch (currencyCode) {
            case "CNY":
                return "¥";
            case "USD":
                return "$";
            case "EUR":
                return "€";
            default:
                return "¥";
        }
    }

    /**
     * 显示自定义样式的AI提示对话框
     */
    private void showAIPromptDialog(String title, String message) {
        try {
            javafx.scene.control.Dialog<Void> dialog = new javafx.scene.control.Dialog<>();
            dialog.setTitle(title);
            dialog.setHeaderText(null);
            
            // 设置对话框内容
            javafx.scene.control.Label contentLabel = new javafx.scene.control.Label(message);
            contentLabel.setWrapText(true);
            contentLabel.setMaxWidth(400);
            contentLabel.setStyle("-fx-font-size: 14px;");
            
            javafx.scene.layout.VBox contentPane = new javafx.scene.layout.VBox(contentLabel);
            contentPane.setPadding(new javafx.geometry.Insets(20, 20, 20, 20));
            contentPane.setStyle("-fx-background-color: #FFFDE7;"); // 轻微黄色背景
            
            dialog.getDialogPane().setContent(contentPane);
            
            // 添加确定按钮
            dialog.getDialogPane().getButtonTypes().add(javafx.scene.control.ButtonType.OK);
            
            // 设置对话框图标和样式
            ((Stage)dialog.getDialogPane().getScene().getWindow()).getIcons().add(
                new javafx.scene.image.Image(getClass().getResourceAsStream("/images/ai_icon.png"))
            );
            
            dialog.showAndWait();
        } catch (Exception e) {
            System.err.println("显示AI提示对话框时出错: " + e.getMessage());
            // 如果自定义对话框显示失败，回退到普通信息对话框
            showInfo(title, message);
        }
    }
}
