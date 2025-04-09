package com.example.loginapp;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 登录和注册界面的控制器类
 */
public class LoginRegisterController {

    // 用于模拟数据存储的用户映射
    private static Map<String, UserAccount> userAccounts = new HashMap<>();

    // 注册界面组件
    @FXML
    private TextField emailPhone;

    @FXML
    private PasswordField password;

    @FXML
    private PasswordField confirmPassword;

    @FXML
    private Label messageLabel;

    @FXML
    private Hyperlink verificationLink;

    /**
     * 初始化控制器
     * 添加一些测试账户
     */
    @FXML
    public void initialize() {
        // 添加一个测试账户（如果还没有添加）
        if (userAccounts.isEmpty()) {
            userAccounts.put("123", new UserAccount("123", "123", "test@example.com"));
        }

        // Add event handler for verification link if it exists (it's only in the login view)
        if (verificationLink != null) {
            verificationLink.setOnAction(this::handleVerificationCode);
        }
    }

    /**
     * 处理注册按钮点击事件
     */
    @FXML
    private void handleRegister(ActionEvent event) {
        String phone = emailPhone.getText().trim();
        String pwd = password.getText().trim();
        String confirmPwd = confirmPassword.getText().trim();

        // 验证输入
        if (phone.isEmpty() || pwd.isEmpty() || confirmPwd.isEmpty()) {
            showMessage("All fields must be filled", true);
            return;
        }

        if (!pwd.equals(confirmPwd)) {
            showMessage("Passwords do not match", true);
            return;
        }

        if (userAccounts.containsKey(phone)) {
            showMessage("Account already exists", true);
            return;
        }

        // 创建新账户
        userAccounts.put(phone, new UserAccount(phone, pwd, ""));
        showMessage("Registration successful!", false);
        clearFields();

        // 切换到登录界面
        navigateToLogin(event);
    }

    /**
     * 处理登录按钮点击事件
     */
    @FXML
    private void handleLogin(ActionEvent event) {
        String phone = emailPhone.getText().trim();
        String pwd = password.getText().trim();

        // 验证输入
        if (phone.isEmpty() || pwd.isEmpty()) {
            showMessage("Please enter phone and password", true);
            return;
        }

        UserAccount account = userAccounts.get(phone);

        if (account == null || !account.getPassword().equals(pwd)) {
            showMessage("Invalid phone or password", true);
            return;
        }

        // 切换到基础视图（包含全局侧边栏）
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/BaseView.fxml"));
            Parent baseView = loader.load();

            // 获取控制器并设置用户名
            BaseViewController baseViewController = loader.getController();
            baseViewController.setUsername(phone);

            // 切换到基础视图
            Scene scene = new Scene(baseView, 1000, 600); // Use consistent dimensions
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showMessage("Error loading application view", true);
        }
    }

    /**
     * 切换到注册界面
     */
    @FXML
    private void switchToRegister(ActionEvent event) {
        try {
            Parent registerView = FXMLLoader.load(getClass().getResource("/fxml/RegisterView.fxml"));
            Scene scene = new Scene(registerView, 1000, 600); // Use consistent dimensions
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 切换到登录界面
     */
    @FXML
    private void navigateToLogin(ActionEvent event) {
        try {
            Parent loginView = FXMLLoader.load(getClass().getResource("/fxml/LoginView.fxml"));
            Scene scene = new Scene(loginView, 1000, 600); // Use consistent dimensions
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handle verification code link click
     */
    @FXML
    private void handleVerificationCode(ActionEvent event) {
        // Show a dialog to enter verification code
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Verification Code");
        dialog.setHeaderText("Enter the verification code sent to your email/phone");
        dialog.setContentText("Code:");

        dialog.showAndWait().ifPresent(code -> {
            if (code.isEmpty()) {
                showMessage("Please enter a verification code", true);
            } else {
                // Here you would validate the verification code
                // For demo purposes, we'll just show a message
                showMessage("Verification code accepted", false);
            }
        });
    }

    /**
     * 显示消息
     */
    private void showMessage(String message, boolean isError) {
        if (messageLabel != null) {
            messageLabel.setText(message);
            messageLabel.setTextFill(isError ? Color.RED : Color.GREEN);
        }
    }

    /**
     * 清空字段
     */
    private void clearFields() {
        emailPhone.clear();
        password.clear();
        if (confirmPassword != null) {
            confirmPassword.clear();
        }
    }

    /**
     * 用户账户类（内部类）
     */
    private static class UserAccount {
        private String username;
        private String password;
        private String email;

        public UserAccount(String username, String password, String email) {
            this.username = username;
            this.password = password;
            this.email = email;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }

        public String getEmail() {
            return email;
        }
    }
}