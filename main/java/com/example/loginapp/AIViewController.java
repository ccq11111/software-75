package com.example.loginapp;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.control.ScrollPane;
import org.springframework.stereotype.Component;

import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.Arrays;
import java.util.List;

/**
 * Controller for the AI View
 */
@Component
public class AIViewController {
    @FXML private Label aiTitleLabel;
    @FXML private VBox messageContainer;
    @FXML private ScrollPane messageScrollPane;
    @FXML private TextField aiInputField;
    @FXML private Button aiSendButton;
    @FXML private Button aiCloseButton;
    @FXML private HBox quickActionsContainer;

    private final RestTemplate restTemplate = new RestTemplate();
    private final String backendEndpoint = "http://localhost:8088/api/ai/chat";

    // Sample quick actions
    private final List<String> quickActions = Arrays.asList(
            "Travel advice", "Assistant", "Festival budget"
    );

    // List to store message history
    private ObservableList<Label> messages = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Set up the quick action buttons
        setupQuickActions();

        // Set up the send button action
        aiSendButton.setOnAction(event -> handleSendMessage());

        // Set up the close button action
        aiCloseButton.setOnAction(event -> closeAIDialog());

        // Set up enter key press in input field
        aiInputField.setOnAction(event -> handleSendMessage());
    }

    /**
     * Set up quick action buttons
     */
    private void setupQuickActions() {
        quickActionsContainer.getChildren().clear();

        for (String action : quickActions) {
            Button actionButton = new Button(action);
            actionButton.getStyleClass().add("ai-quick-action");
            actionButton.setOnAction(event -> {
                // Add user message directly
                addUserMessage(action);
                aiInputField.clear();
                // Generate AI response with typing effect
                generateAIResponse(action);
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

        // Generate AI response with typing effect
        generateAIResponse(message);
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
     * Generate an AI response with typing effect
     */
    private void generateAIResponse(String userMessage) {
        // Create the AI message label
        final Label aiMessage = new Label("");
        aiMessage.getStyleClass().add("ai-message");
        aiMessage.setMaxWidth(600);
        aiMessage.setWrapText(true);

        final HBox messageBox = new HBox(aiMessage);
        messageBox.setAlignment(Pos.CENTER_LEFT);

        messageContainer.getChildren().add(messageBox);

        // Get response from backend in a background thread
        new Thread(() -> {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                
                HttpEntity<String> request = new HttpEntity<>(userMessage, headers);
                final String fullResponse = restTemplate.postForObject(backendEndpoint, request, String.class);
                
                // Post-process the response
                final String processedResponse = fullResponse
                    .replaceAll("(?s)<think>.*?</think>", "") // 使用(?s)使.能匹配换行符
                    .replaceAll("(?s)<think>.*</think>", "")  // 处理没有闭合标签的情况
                    .replaceAll("```.*?```", "")
                    .replaceAll("`.*?`", "")
                    .replaceAll("\\*\\*.*?\\*\\*", "")
                    .replaceAll("\\*.*?\\*", "")
                    .replaceAll("#{1,6}\\s", "")
                    .replaceAll("\\[.*?\\]\\(.*?\\)", "")
                    .replaceAll(">\\s", "")
                    .replaceAll("-\\s", "")
                    .replaceAll("\\d+\\.\\s", "")
                    .trim();

                // Update UI on JavaFX thread with typing effect
                Platform.runLater(() -> {
                    // Simulate typing effect
                    final int[] charIndex = {0};
                    Timeline timeline = new Timeline(
                            new KeyFrame(Duration.millis(10), event -> {
                                if (charIndex[0] < processedResponse.length()) {
                                    aiMessage.setText(processedResponse.substring(0, ++charIndex[0]));

                                    // Scroll to bottom as text is added
                                    messageContainer.layout();
                                    messageScrollPane.setVvalue(1.0);
                                }
                            })
                    );
                    timeline.setCycleCount(processedResponse.length());
                    timeline.play();
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    aiMessage.setText("Sorry, I encountered an error while processing your request.");
                });
            }
        }).start();
    }

    /**
     * Close the AI dialog
     */
    private void closeAIDialog() {
        Stage stage = (Stage) aiCloseButton.getScene().getWindow();
        stage.close();
    }
}