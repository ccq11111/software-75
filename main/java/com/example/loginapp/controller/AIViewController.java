// Unified controller template upgrade: all controllers now implement TokenAwareController

package com.example.loginapp.controller;

import com.example.loginapp.service.JwtSecretKeyService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class AIViewController implements TokenAwareController {
    @FXML private Label aiTitleLabel;
    @FXML private VBox messageContainer;
    @FXML private ScrollPane messageScrollPane;
    @FXML private TextField aiInputField;
    @FXML private Button aiSendButton;
    @FXML private Button aiCloseButton;
    @FXML private HBox quickActionsContainer;

    private final RestTemplate restTemplate;
    private final JwtSecretKeyService jwtService;
    
    @Value("${ai.backend.url:http://localhost:8088}")
    private String backendBaseUrl;
    
    private final String backendEndpoint = "/api/ai/chat";

    private final List<String> quickActions = Arrays.asList("Travel advice", "Assistant", "Festival budget");
    private final ObservableList<Label> messages = FXCollections.observableArrayList();

    private String authToken;

    @Autowired
    public AIViewController(JwtSecretKeyService jwtService) {
        this.jwtService = jwtService;
        this.restTemplate = new RestTemplate();
    }

    @Override
    public void setAuthToken(String token) {
        this.authToken = token;
    }

    @FXML
    public void initialize() {
        setupQuickActions();
        aiSendButton.setOnAction(event -> handleSendMessage());
        aiCloseButton.setOnAction(event -> handleClose());
        aiInputField.setOnAction(event -> handleSendMessage());
    }

    private void setupQuickActions() {
        quickActionsContainer.getChildren().clear();
        for (String action : quickActions) {
            Button actionButton = new Button(action);
            actionButton.getStyleClass().add("ai-quick-action");
            actionButton.setOnAction(event -> {
                addUserMessage(action);
                aiInputField.clear();
                generateAIResponse(action);
            });
            quickActionsContainer.getChildren().add(actionButton);
        }
    }

    private void handleSendMessage() {
        String message = aiInputField.getText().trim();
        if (message.isEmpty()) return;
        addUserMessage(message);
        aiInputField.clear();
        generateAIResponse(message);
    }

    private void addUserMessage(String message) {
        Label userMessage = new Label(message);
        userMessage.getStyleClass().add("user-message");
        userMessage.setMaxWidth(600);
        userMessage.setWrapText(true);
        HBox messageBox = new HBox(userMessage);
        messageBox.setAlignment(Pos.CENTER_RIGHT);
        messageContainer.getChildren().add(messageBox);
    }

    private void generateAIResponse(String userMessage) {
        Label aiMessage = new Label("");
        aiMessage.getStyleClass().add("ai-message");
        aiMessage.setMaxWidth(600);
        aiMessage.setWrapText(true);
        HBox messageBox = new HBox(aiMessage);
        messageBox.setAlignment(Pos.CENTER_LEFT);
        messageContainer.getChildren().add(messageBox);

        new Thread(() -> {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("Authorization", "Bearer " + authToken);

                HttpEntity<String> request = new HttpEntity<>(userMessage, headers);
                String fullResponse = restTemplate.postForObject(backendBaseUrl + backendEndpoint, request, String.class);

                String processedResponse = fullResponse
                        .replaceAll("(?s)<think>.*?</think>", "")
                        .replaceAll("(?s)<think>.*</think>", "")
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

                Platform.runLater(() -> simulateTypingEffect(processedResponse, aiMessage));
            } catch (HttpClientErrorException.Unauthorized e) {
                Platform.runLater(() -> {
                    aiMessage.setText("Authentication failed. Please login again.");
                    aiMessage.getStyleClass().add("error-message");
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    aiMessage.setText("Sorry, I encountered an error while processing your request.");
                    aiMessage.getStyleClass().add("error-message");
                });
            }
        }).start();
    }

    private void simulateTypingEffect(String response, Label aiMessage) {
        final int[] charIndex = {0};
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(10), event -> {
                    if (charIndex[0] < response.length()) {
                        aiMessage.setText(response.substring(0, ++charIndex[0]));
                        messageContainer.layout();
                        messageScrollPane.setVvalue(1.0);
                    }
                })
        );
        timeline.setCycleCount(response.length());
        timeline.play();
    }

    private void handleClose() {
        Stage stage = (Stage) aiCloseButton.getScene().getWindow();
        stage.close();
    }
}
