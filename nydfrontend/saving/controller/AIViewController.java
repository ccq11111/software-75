package com.example.saving.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

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

        // Get response from backend in an asynchronous way using CompletableFuture
        long startTime = System.currentTimeMillis(); // Record start time for debugging
        CompletableFuture.supplyAsync(() -> {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                HttpEntity<String> request = new HttpEntity<>(userMessage, headers);
                System.out.println("Sending request to backend...");
                long requestStartTime = System.currentTimeMillis(); // Record time before sending request
                final String fullResponse = restTemplate.postForObject(backendEndpoint, request, String.class);
                long requestEndTime = System.currentTimeMillis(); // Record time after response received
                System.out.println("Request took " + (requestEndTime - requestStartTime) + " ms");

                // Post-process the response
                return processResponse(fullResponse);
            } catch (Exception e) {
                e.printStackTrace();
                return "Sorry, I encountered an error while processing your request.";
            }
        }).thenAccept(response -> {
            // Print the total time for the entire process
            long endTime = System.currentTimeMillis(); // Record end time for debugging
            System.out.println("Total time taken for response: " + (endTime - startTime) + " ms");

            // Update UI on JavaFX thread with typing effect
            Platform.runLater(() -> {
                simulateTypingEffect(response, aiMessage);
            });
        });
    }

    /**
     * Process the AI response (clean the unnecessary parts)
     */
    private String processResponse(String response) {
        return response
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
    }

    /**
     * Simulate typing effect for AI response
     */
    private void simulateTypingEffect(String response, Label aiMessage) {
        final int[] charIndex = {0};
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(10), event -> {
                    if (charIndex[0] < response.length()) {
                        aiMessage.setText(response.substring(0, ++charIndex[0]));

                        // Scroll to bottom as text is added
                        messageContainer.layout();
                        messageScrollPane.setVvalue(1.0);
                    }
                })
        );
        timeline.setCycleCount(response.length());
        timeline.play();
    }

    /**
     * Close the AI dialog
     */
    private void closeAIDialog() {
        Stage stage = (Stage) aiCloseButton.getScene().getWindow();
        stage.close();
    }
}
