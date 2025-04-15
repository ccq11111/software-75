package com.example.loginapp;

import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.springframework.stereotype.Controller;

@Controller
public class LoginRegisterController {
    @FXML
    private TextField emailPhone;

    @FXML
    private PasswordField password;

    @FXML
    private Hyperlink verificationLink;

    @FXML
    private Hyperlink signUpLink;

    @FXML
    private void handleLogin() {
        // TODO: 实现登录逻辑
        System.out.println("Login clicked");
    }

    @FXML
    private void switchToRegister() {
        // TODO: 切换到注册界面
        System.out.println("Switch to register");
    }
} 