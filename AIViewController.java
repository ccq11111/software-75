package com.example.software.view;

import com.example.software.api.*;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableCell;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;

/**
 * Controller for the AI View
 */
public class AIViewController {
    @FXML private Label aiTitleLabel;
    @FXML private VBox messageContainer;
    @FXML private ScrollPane messageScrollPane;
    @FXML private TextField aiInputField;
    @FXML private Button aiSendButton;
    @FXML private Button aiCloseButton;
    @FXML private HBox quickActionsContainer;
    @FXML private VBox rootVBox;
    private double xOffset = 0;
    private double yOffset = 0;

    // API service factory
    private final ApiServiceFactory apiServiceFactory = ApiServiceFactory.getInstance();

    // Quick actions
    private List<QuickAction> quickActions = new ArrayList<>();

    // Sample AI responses for fallback
    private final List<String> fallbackResponses = List.of(
            "OK! According to the billing data you recorded, with an annual income of $300,000 and annual expenses of $150,000, your average monthly expenses are about $12,500. As an important festival in China, the expenditure of the Spring Festival is usually higher than usual, mainly including the following aspects:\n\n1.Gifts and red envelopes: Spring Festival is a peak time for gifts and red envelopes, especially for family members, relatives and friends. Depending on your income level, you can expect to spend between $10,000 and $20,000, depending on the number and amount of money you need to give out.\n\n2.Food and meals: Family meals and purchases are common expenses during the Spring Festival. Expect to spend between $5,000 and $10,000 depending on the size of your family and the frequency of meals.\n\n3.Travel and entertainment: If you plan to travel or participate in entertainment activitie during the Chinese New Year, this part of the expenditure may be higher. It is expected that the cost of the trip can be between 10,000 and 30,000, depending on the distance to the destination and the method of travel.",
            "Based on your spending patterns, I recommend setting aside about 15% of your monthly income for savings. This would be approximately ¥3,750 per month based on your current income level.",
            "Looking at your transaction history, your largest expense categories are housing (35%), food (25%), and transportation (15%). You might want to consider reducing your dining out expenses, which account for 60% of your food budget.",
            "I've analyzed your investment portfolio and noticed it's heavily weighted towards technology stocks (65%). For better diversification, consider allocating more to other sectors like healthcare and consumer staples."
    );



    @FXML
    public void initialize() {
        // Load quick actions
        loadQuickActions();

        // Set up the send button action
        aiSendButton.setOnAction(event -> handleSendMessage());

        // Set up the close button action
        aiCloseButton.setOnAction(event -> closeAIDialog());

        // Set up enter key press in input field
        aiInputField.setOnAction(event -> handleSendMessage());

        // 鼠标拖拽窗口
        rootVBox.setOnMousePressed(event -> {
            Stage stage = (Stage) rootVBox.getScene().getWindow();
            xOffset = stage.getX() - event.getScreenX();
            yOffset = stage.getY() - event.getScreenY();
        });
        rootVBox.setOnMouseDragged(event -> {
            Stage stage = (Stage) rootVBox.getScene().getWindow();
            stage.setX(event.getScreenX() + xOffset);
            stage.setY(event.getScreenY() + yOffset);
        });
    }

    /**
     * Load quick actions from the API
     */
    private void loadQuickActions() {
        try {
            // Get the AI service
            AIService aiService = apiServiceFactory.getAIService();

            // Get quick actions
            quickActions = aiService.getQuickActions();

            // Set up quick action buttons
            setupQuickActionButtons();
        } catch (ApiException e) {
            // If there's an error, use default quick actions
            quickActions = List.of(
                new QuickAction("budget", "Holiday Analysis"),
                new QuickAction("save", "Tourism Analysis"),
                new QuickAction("invest", "Consumption Patterns"),
                new QuickAction("remind", "Periodic Reminders")
            );
            setupQuickActionButtons();
            System.err.println("Error loading quick actions: " + e.getMessage());
        }
    }

    /**
     * Set up quick action buttons
     */
    private void setupQuickActionButtons() {
        quickActionsContainer.getChildren().clear();

        // 节日消费分析按钮
        Button holidayBtn = new Button("节日消费分析");
        holidayBtn.getStyleClass().add("ai-quick-action");
        holidayBtn.setOnAction(event -> {
            addUserMessage("节日消费分析");
            new Thread(this::getHolidayAdvice).start();
        });
        
        // 旅游消费规划按钮
        Button tourismBtn = new Button("旅游消费规划");
        tourismBtn.getStyleClass().add("ai-quick-action");
        tourismBtn.setOnAction(event -> {
            addUserMessage("旅游消费规划");
            Platform.runLater(this::askTourismDestination);
        });
        
        // 消费模式分析按钮
        Button consumeAnalysisBtn = new Button("消费模式分析");
        consumeAnalysisBtn.getStyleClass().add("ai-quick-action");
        consumeAnalysisBtn.setOnAction(e -> {
            addUserMessage("消费模式分析");
            new Thread(this::getConsumeAnalysis).start();
        });
        
        // 记一笔消费按钮
        Button recordBtn = new Button("记一笔消费");
        recordBtn.getStyleClass().add("ai-quick-action");
        recordBtn.setOnAction(e -> {
            addUserMessage("请输入您的消费记录");
            Platform.runLater(() -> {
                displayAIResponse("请描述您的消费情况，例如：今天在超市买水果花了50元");
                aiInputField.setPromptText("描述您的消费情况...");
                aiInputField.requestFocus();
            });
        });
        
        // 周期性提醒按钮
        Button periodicReminderBtn = new Button("周期性提醒");
        periodicReminderBtn.getStyleClass().add("ai-quick-action");
        periodicReminderBtn.setOnAction(e -> {
            addUserMessage("周期性交易提醒");
            new Thread(this::getPeriodicReminders).start();
        });
        
        // 添加所有按钮到容器
        quickActionsContainer.getChildren().addAll(holidayBtn, tourismBtn, consumeAnalysisBtn, recordBtn, periodicReminderBtn);
    }

    /**
     * Handle sending a message
     */
    private void handleSendMessage() {
        String message = aiInputField.getText().trim();
        if (message.isEmpty()) return;

        addUserMessage(message);
        aiInputField.clear();

        // 判断是否为消费记录
        if (isConsumeRecord(message)) {
            new Thread(() -> sendConsumeRecord(message)).start();
        } else {
            new Thread(() -> {
                getAIAdvice(message);
            }).start();
        }
    }

    /**
     * 判断输入文本是否为消费记录
     * 通过关键词匹配和正则表达式确定
     */
    private boolean isConsumeRecord(String text) {
        // 包含消费关键词
        boolean hasConsumeKeyword = text.matches(".*(买|花了|消费|支付|付款|元|购买|价格|花费|花钱|人民币|块|成本).*");
        
        // 包含金额模式
        boolean hasAmount = text.matches(".*\\d+(\\.\\d+)?(元|块|块钱|人民币)?.*");
        
        // 排除明显的查询消息而非记账消息
        boolean notQuery = !text.matches(".*(怎么|如何|多少|什么|是否|能不能|请问|查询|分析).*(\\?|？)");
        
        return hasConsumeKeyword && hasAmount && notQuery;
    }

    /**
     * Add a user message to the chat
     */
    private void addUserMessage(String message) {
        Label userMessage = new Label(message);
        userMessage.getStyleClass().add("user-message");
        userMessage.setMaxWidth(600);
        userMessage.setWrapText(true);
        // 时间戳
        Label timeLabel = new Label(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        timeLabel.getStyleClass().add("message-timestamp");
        VBox vbox = new VBox(userMessage, timeLabel);
        vbox.setAlignment(Pos.CENTER_RIGHT);
        HBox messageBox = new HBox(vbox);
        messageBox.setAlignment(Pos.CENTER_RIGHT);
        messageContainer.getChildren().add(messageBox);
    }

    /**
     * Get AI advice from the API
     */
    private void getAIAdvice(String message) {
        try {
            // Get the AI service
            AIService aiService = apiServiceFactory.getAIService();

            // Get AI advice
            AIAdviceResponse response = aiService.getAdvice(message, true, true, true);

            // Display the AI response with typing effect
            Platform.runLater(() -> {
                displayAIResponse(response.getMessage());
            });


            // Display suggestions if available
            if (response.getSuggestions() != null && !response.getSuggestions().isEmpty()) {
                for (AISuggestion suggestion : response.getSuggestions()) {
                    // Add a small delay before showing each suggestion
                    Timeline delay = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
                        displayAISuggestion(suggestion.getText());
                    }));
                    delay.play();
                }
            }
        } catch (ApiException e) {
            // If there's an error, use a fallback response
            Random random = new Random();
            String fallbackResponse = fallbackResponses.get(random.nextInt(fallbackResponses.size()));
            displayAIResponse(fallbackResponse);
            System.err.println("Error getting AI advice: " + e.getMessage());
        }
    }

    /**
     * Display an AI response with typing effect
     */
    private void displayAIResponse(String fullResponse) {
        Label aiMessage = new Label("");
        aiMessage.getStyleClass().add("ai-message");
        aiMessage.setMaxWidth(600);
        aiMessage.setWrapText(true);
        // 时间戳
        Label timeLabel = new Label(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        timeLabel.getStyleClass().add("message-timestamp");
        VBox vbox = new VBox(aiMessage, timeLabel);
        vbox.setAlignment(Pos.CENTER_LEFT);
        HBox messageBox = new HBox(vbox);
        messageBox.setAlignment(Pos.CENTER_LEFT);
        messageContainer.getChildren().add(messageBox);
        
        // 检查是否包含Markdown表格
        if (fullResponse.contains("| 类别 | 产品 |") && fullResponse.contains("|------|------|")) {
            // 表格在第二部分显示，第一部分先打字机效果显示
            int tableStart = fullResponse.indexOf("| 类别 ");
            String introText = fullResponse.substring(0, tableStart).trim();
            String tableText = fullResponse.substring(tableStart);
            
            // 先显示介绍文字
            final int[] charIndex = {0};
            Timeline timeline = new Timeline(
                    new KeyFrame(Duration.millis(10), event -> {
                        if (charIndex[0] < introText.length()) {
                            aiMessage.setText(introText.substring(0, ++charIndex[0]));
                            Platform.runLater(() -> {
                                messageContainer.layout();
                                messageScrollPane.setVvalue(1.0);
                            });
                        }
                    })
            );
            timeline.setCycleCount(introText.length());
            timeline.setOnFinished(e -> {
                // 显示完介绍文字后，创建并显示表格
                Platform.runLater(() -> {
                    // 创建表格视图
                    TableView<PeriodicReminderRow> tableView = createPeriodicRemindersTable(tableText);
                    tableView.setMaxWidth(580);
                    tableView.setMaxHeight(150);
                    
                    // 添加表格和附加说明
                    VBox tableContainer = new VBox(5);
                    tableContainer.getChildren().add(tableView);
                    
                    // 提取注意事项
                    int noteStart = tableText.lastIndexOf("\n\n");
                    if (noteStart >= 0) {
                        String note = tableText.substring(noteStart).trim();
                        Label noteLabel = new Label(note);
                        noteLabel.setWrapText(true);
                        noteLabel.getStyleClass().add("ai-message");
                        tableContainer.getChildren().add(noteLabel);
                    }
                    
                    HBox tableBox = new HBox(tableContainer);
                    tableBox.setAlignment(Pos.CENTER_LEFT);
                    messageContainer.getChildren().add(tableBox);
                    
                    // 滚动到底部
                    messageContainer.layout();
                    messageScrollPane.setVvalue(1.0);
                });
            });
            timeline.play();
        } else {
            // 普通消息，使用打字机效果
            final int[] charIndex = {0};
            Timeline timeline = new Timeline(
                    new KeyFrame(Duration.millis(10), event -> {
                        if (charIndex[0] < fullResponse.length()) {
                            aiMessage.setText(fullResponse.substring(0, ++charIndex[0]));
                            Platform.runLater(() -> {
                                messageContainer.layout();
                                messageScrollPane.setVvalue(1.0);
                            });
                        }
                    })
            );
            timeline.setCycleCount(fullResponse.length());
            timeline.play();
        }
    }

    /**
     * Display an AI suggestion
     */
    private void displayAISuggestion(String suggestion) {
        Label suggestionLabel = new Label(suggestion);
        suggestionLabel.getStyleClass().addAll("ai-message", "ai-suggestion");
        suggestionLabel.setMaxWidth(600);
        suggestionLabel.setWrapText(true);

        HBox messageBox = new HBox(suggestionLabel);
        messageBox.setAlignment(Pos.CENTER_LEFT);

        messageContainer.getChildren().add(messageBox);

        // Scroll to bottom
        Platform.runLater(() -> {
            messageContainer.layout();
            messageScrollPane.setVvalue(1.0);
        });
    }

    /**
     * Close the AI dialog
     */
    private void closeAIDialog() {
        Stage stage = (Stage) aiCloseButton.getScene().getWindow();
        stage.close();
    }

    /**
     * 节日消费分析：调用后端holiday-advice接口并展示结果
     */
    private void getHolidayAdvice() {
        try {
            AIService aiService = apiServiceFactory.getAIService();
            // 默认账单路径，可根据实际情况调整
            String csvPath = "data/billing/billingEntries.csv";
            AIAdviceResponse response = aiService.getHolidayAdvice(csvPath);
            Platform.runLater(() -> displayAIResponse(response.getMessage()));
        } catch (ApiException e) {
            Platform.runLater(() -> displayAIResponse("节日消费分析失败：" + e.getMessage()));
        }
    }

    private void askTourismDestination() {
        displayAIResponse("你想去哪里旅游？请输入目的地城市名称。");
        aiInputField.setPromptText("请输入旅游目的地...");
        aiInputField.setOnAction(event -> {
            String city = aiInputField.getText().trim();
            if (!city.isEmpty()) {
                addUserMessage(city);
                aiInputField.clear();
                aiInputField.setPromptText("Ask anything...");
                aiInputField.setOnAction(e -> handleSendMessage()); // 恢复默认行为
                new Thread(() -> getTourismAdvice(city)).start();
            }
        });
    }

    private void getTourismAdvice(String city) {
        try {
            AIService aiService = apiServiceFactory.getAIService();
            String advice = aiService.getTourismAdvice(city).getMessage();
            Platform.runLater(() -> displayAIResponse(advice));
        } catch (ApiException e) {
            Platform.runLater(() -> displayAIResponse("旅游消费建议获取失败：" + e.getMessage()));
        }
    }

    private void getConsumeAnalysis() {
        try {
            AIService aiService = apiServiceFactory.getAIService();
            String result = aiService.get("/ai/consume-analysis");
            Platform.runLater(() -> displayAIResponse(result));
        } catch (Exception e) {
            Platform.runLater(() -> displayAIResponse("消费模式分析失败：" + e.getMessage()));
        }
    }

    /**
     * 获取周期性交易提醒
     */
    private void getPeriodicReminders() {
        try {
            Platform.runLater(() -> {
                Label processingLabel = new Label("正在分析周期性交易模式...");
                processingLabel.getStyleClass().add("ai-message");
                HBox messageBox = new HBox(processingLabel);
                messageBox.setAlignment(Pos.CENTER_LEFT);
                messageContainer.getChildren().add(messageBox);
                // 滚动到底部
                messageContainer.layout();
                messageScrollPane.setVvalue(1.0);
            });
            
            AIService aiService = apiServiceFactory.getAIService();
            String result = aiService.get("/ai/periodic-reminders");
            
            Platform.runLater(() -> {
                // 移除处理中消息
                messageContainer.getChildren().remove(messageContainer.getChildren().size() - 1);
                displayAIResponse(result);
            });
        } catch (Exception e) {
            Platform.runLater(() -> {
                // 移除处理中消息
                if (!messageContainer.getChildren().isEmpty()) {
                    messageContainer.getChildren().remove(messageContainer.getChildren().size() - 1);
                }
                displayAIResponse("周期性交易分析失败：" + e.getMessage());
            });
        }
    }

    /**
     * 发送消费记录到服务器
     */
    private void sendConsumeRecord(String record) {
        try {
            // 显示处理中消息
            Platform.runLater(() -> {
                Label processingLabel = new Label("正在处理消费记录...");
                processingLabel.getStyleClass().add("ai-message");
                HBox messageBox = new HBox(processingLabel);
                messageBox.setAlignment(Pos.CENTER_LEFT);
                messageContainer.getChildren().add(messageBox);
                // 滚动到底部
                messageContainer.layout();
                messageScrollPane.setVvalue(1.0);
            });

            // 调用AI服务处理消费记录
            AIService aiService = apiServiceFactory.getAIService();
            String result = aiService.postConsumeRecord(record);
            
            // 显示处理结果
            Platform.runLater(() -> {
                // 移除处理中消息
                messageContainer.getChildren().remove(messageContainer.getChildren().size() - 1);
                displayAIResponse(result);
                
                // 尝试刷新账单视图
                refreshBillingView();
            });
        } catch (Exception e) {
            Platform.runLater(() -> {
                // 移除处理中消息
                messageContainer.getChildren().remove(messageContainer.getChildren().size() - 1);
                displayAIResponse("消费记录入账失败：" + e.getMessage());
            });
        }
    }

    /**
     * 尝试刷新账单视图
     * 如果账单视图控制器在当前应用程序中活跃，则通知其刷新数据
     */
    private void refreshBillingView() {
        try {
            // 获取主应用程序上下文
            javafx.stage.Window window = rootVBox.getScene().getWindow();
            if (window instanceof javafx.stage.Stage) {
                javafx.stage.Stage stage = (javafx.stage.Stage) window;
                if (stage.getOwner() instanceof javafx.stage.Stage) {
                    javafx.stage.Stage mainStage = (javafx.stage.Stage) stage.getOwner();
                    
                    // 尝试获取主场景并查找BillingViewController实例
                    if (mainStage.getScene() != null) {
                        // 尝试在当前场景中查找BillingViewController的实例
                        for (javafx.scene.Node node : mainStage.getScene().getRoot().lookupAll(".root")) {
                            if (node.getUserData() instanceof BillingViewController) {
                                // 直接调用公共方法刷新数据
                                BillingViewController controller = (BillingViewController) node.getUserData();
                                Platform.runLater(controller::loadBillingData);
                                System.out.println("已触发账单视图刷新");
                                break;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("尝试刷新账单视图时出错: " + e.getMessage());
        }
    }

    /**
     * 从Markdown表格文本创建JavaFX表格视图
     */
    private TableView<PeriodicReminderRow> createPeriodicRemindersTable(String tableText) {
        TableView<PeriodicReminderRow> tableView = new TableView<>();
        tableView.getStyleClass().add("periodic-reminders-table");
        
        // 创建列
        TableColumn<PeriodicReminderRow, String> categoryCol = new TableColumn<>("类别");
        categoryCol.setCellValueFactory(data -> data.getValue().categoryProperty());
        
        TableColumn<PeriodicReminderRow, String> productCol = new TableColumn<>("产品");
        productCol.setCellValueFactory(data -> data.getValue().productProperty());
        
        TableColumn<PeriodicReminderRow, Double> amountCol = new TableColumn<>("金额");
        amountCol.setCellValueFactory(data -> data.getValue().amountProperty().asObject());
        amountCol.setCellFactory(column -> new TableCell<PeriodicReminderRow, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f", item));
                }
            }
        });
        
        TableColumn<PeriodicReminderRow, Integer> intervalCol = new TableColumn<>("周期(天)");
        intervalCol.setCellValueFactory(data -> data.getValue().intervalProperty().asObject());
        
        TableColumn<PeriodicReminderRow, String> nextDateCol = new TableColumn<>("预计日期");
        nextDateCol.setCellValueFactory(data -> data.getValue().nextDateProperty());
        
        TableColumn<PeriodicReminderRow, Integer> daysCol = new TableColumn<>("剩余天数");
        daysCol.setCellValueFactory(data -> data.getValue().daysUntilNextProperty().asObject());
        daysCol.setCellFactory(column -> new TableCell<PeriodicReminderRow, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toString());
                    if (item <= 7) {
                        setStyle("-fx-text-fill: #F44336; -fx-font-weight: bold;"); // 红色，即将发生
                    } else if (item <= 14) {
                        setStyle("-fx-text-fill: #FF9800; -fx-font-weight: bold;"); // 橙色，即将到来
                    } else {
                        setStyle("-fx-text-fill: #4CAF50;"); // 绿色，尚有时间
                    }
                }
            }
        });
        
        // 设置列宽
        categoryCol.setPrefWidth(80);
        productCol.setPrefWidth(120);
        amountCol.setPrefWidth(70);
        intervalCol.setPrefWidth(70);
        nextDateCol.setPrefWidth(100);
        daysCol.setPrefWidth(70);
        
        tableView.getColumns().addAll(categoryCol, productCol, amountCol, intervalCol, nextDateCol, daysCol);
        
        // 从表格文本解析数据
        List<PeriodicReminderRow> rows = parsePeriodicRemindersTable(tableText);
        tableView.getItems().addAll(rows);
        
        return tableView;
    }

    /**
     * 从Markdown表格文本解析数据行
     */
    private List<PeriodicReminderRow> parsePeriodicRemindersTable(String tableText) {
        List<PeriodicReminderRow> rows = new ArrayList<>();
        
        // 按行分割
        String[] lines = tableText.split("\n");
        
        // 跳过表头和分隔行
        for (int i = 2; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty() || !line.startsWith("|")) continue;
            
            // 解析每一行
            String[] cells = line.split("\\|");
            if (cells.length < 7) continue;
            
            try {
                String category = cells[1].trim();
                String product = cells[2].trim();
                double amount = Double.parseDouble(cells[3].trim());
                int interval = Integer.parseInt(cells[4].trim());
                String nextDate = cells[5].trim();
                int daysUntil = Integer.parseInt(cells[6].trim());
                
                rows.add(new PeriodicReminderRow(category, product, amount, interval, nextDate, daysUntil));
            } catch (NumberFormatException e) {
                // 忽略解析错误
                continue;
            }
        }
        
        return rows;
    }

    /**
     * 周期性提醒表格行数据类
     */
    private static class PeriodicReminderRow {
        private final StringProperty category = new SimpleStringProperty();
        private final StringProperty product = new SimpleStringProperty();
        private final DoubleProperty amount = new SimpleDoubleProperty();
        private final IntegerProperty interval = new SimpleIntegerProperty();
        private final StringProperty nextDate = new SimpleStringProperty();
        private final IntegerProperty daysUntilNext = new SimpleIntegerProperty();
        
        public PeriodicReminderRow(String category, String product, double amount, 
                                  int interval, String nextDate, int daysUntilNext) {
            this.category.set(category);
            this.product.set(product);
            this.amount.set(amount);
            this.interval.set(interval);
            this.nextDate.set(nextDate);
            this.daysUntilNext.set(daysUntilNext);
        }
        
        public StringProperty categoryProperty() { return category; }
        public StringProperty productProperty() { return product; }
        public DoubleProperty amountProperty() { return amount; }
        public IntegerProperty intervalProperty() { return interval; }
        public StringProperty nextDateProperty() { return nextDate; }
        public IntegerProperty daysUntilNextProperty() { return daysUntilNext; }
    }
}
