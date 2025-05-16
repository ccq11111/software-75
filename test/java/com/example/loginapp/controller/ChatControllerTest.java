package com.example.loginapp.controller;

import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.ApplicationTest;
import org.merak.aidemo.AiSpringBootApp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(ApplicationExtension.class)
@SpringBootTest(classes = AiSpringBootApp.class)
@ActiveProfiles("test")
public class ChatControllerTest extends ApplicationTest {

    private ChatController controller;
    private TextArea chatArea;
    private TextField messageField;

    @BeforeEach
    public void setup() {
        controller = new ChatController();
        chatArea = new TextArea();
        messageField = new TextField();

        controller.setChatArea(chatArea);
        controller.setMessageField(messageField);
    }

    @Test
    public void testSendMessage() {
        // 设置测试消息
        messageField.setText("Hello");

        // 执行发送消息
        controller.sendMessage();

        // 验证消息被添加到聊天区域
        String chatText = chatArea.getText();
        assertTrue(chatText.contains("You: Hello"));

        // 验证输入框被清空
        assertEquals("", messageField.getText());
    }
} 