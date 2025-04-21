package com.example.loginapp;

import com.example.loginapp.api.*;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.control.ScrollPane;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
                new QuickAction("budget", "How can I budget better?"),
                new QuickAction("save", "Tips for saving money"),
                new QuickAction("invest", "Investment advice")
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

        for (QuickAction action : quickActions) {
            Button actionButton = new Button(action.getText());
            actionButton.getStyleClass().add("ai-quick-action");
            actionButton.setOnAction(event -> {
                aiInputField.setText(action.getText());
                handleSendMessage();
            });
            quickActionsContainer.getChildren().add(actionButton);
        }
    }

    /**
     * Handle sending a message
     */
    private void handleSendMessage() {
        String message = aiInputField.getText().trim();
        if (message.isEmpty()) return;

        // Add user message
        addUserMessage(message);

        // Clear input field
        aiInputField.clear();

        // Get AI advice
        getAIAdvice(message);
    }

    /**
     * Add a user message to the chat
     */
    private void addUserMessage(String message) {
        Label userMessage = new Label(message);
        userMessage.getStyleClass().add("user-message");
        userMessage.setMaxWidth(600);
        userMessage.setWrapText(true);

        HBox messageBox = new HBox(userMessage);
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
            displayAIResponse(response.getMessage());

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
        // Create the AI message label
        Label aiMessage = new Label("");
        aiMessage.getStyleClass().add("ai-message");
        aiMessage.setMaxWidth(600);
        aiMessage.setWrapText(true);

        HBox messageBox = new HBox(aiMessage);
        messageBox.setAlignment(Pos.CENTER_LEFT);

        messageContainer.getChildren().add(messageBox);

        // Simulate typing effect
        final int[] charIndex = {0};
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(10), event -> {
                    if (charIndex[0] < fullResponse.length()) {
                        aiMessage.setText(fullResponse.substring(0, ++charIndex[0]));

                        // Scroll to bottom as text is added
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
}
