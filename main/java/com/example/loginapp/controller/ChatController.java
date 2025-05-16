package com.example.loginapp.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.springframework.stereotype.Controller;
import org.springframework.web.client.RestTemplate;

@Controller
public class ChatController {
    @FXML
    private TextArea chatArea;

    @FXML
    private TextField messageField;

    private final RestTemplate restTemplate = new RestTemplate();
    private final String apiUrl = "http://localhost:8088/api/ai/chat";

    public TextArea getChatArea() {
        return chatArea;
    }

    public void setChatArea(TextArea chatArea) {
        this.chatArea = chatArea;
    }

    public TextField getMessageField() {
        return messageField;
    }

    public void setMessageField(TextField messageField) {
        this.messageField = messageField;
    }

    @FXML
    public void sendMessage() {
        String message = messageField.getText();
        if (!message.isEmpty()) {
            // 显示用户消息
            chatArea.appendText("You: " + message + "\n");

            // 发送到后端并获取响应
            String response = restTemplate.postForObject(apiUrl, message, String.class);

            // 显示 AI 响应
            chatArea.appendText("AI: " + response + "\n\n");

            // 清空输入框
            messageField.clear();
        }
    }
} 