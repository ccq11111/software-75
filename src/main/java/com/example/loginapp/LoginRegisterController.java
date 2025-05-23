package com.example.loginapp;

import com.example.loginapp.api.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;

/**
 * 登录和注册界面的控制器类
 */
public class LoginRegisterController {

    // API service factory
    private final ApiServiceFactory apiServiceFactory = ApiServiceFactory.getInstance();

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
     */
    @FXML
    public void initialize() {
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
        String username = emailPhone.getText().trim();
        String pwd = password.getText().trim();
        String confirmPwd = confirmPassword.getText().trim();

        // Get the window for notifications
        Window window = emailPhone.getScene().getWindow();

        // 验证输入
        if (username.isEmpty() || pwd.isEmpty() || confirmPwd.isEmpty()) {
            showMessage("All fields must be filled", true);
            NotificationManager.showError(window, "Registration Error", "All fields must be filled");
            return;
        }

        if (!pwd.equals(confirmPwd)) {
            showMessage("Passwords do not match", true);
            NotificationManager.showError(window, "Registration Error", "Passwords do not match");
            return;
        }

        try {
            // 调用注册API
            AuthService authService = apiServiceFactory.getAuthService();
            RegistrationResponse response = authService.register(username, pwd, "", "");
            
            if (response.isSuccess()) {
                showMessage("Registration successful!", false);
                NotificationManager.showSuccess(window, "Registration Successful", "Your account has been created successfully!");
                clearFields();
                // 切换到登录界面
                navigateToLogin(event);
            } else {
                showMessage(response.getMessage(), true);
                NotificationManager.showError(window, "Registration Error", response.getMessage());
            }
        } catch (ApiException e) {
            System.err.println("Registration error: " + e.getMessage());
            e.printStackTrace();
            showMessage(e.getMessage(), true);
            NotificationManager.showError(window, "Registration Error", e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error during registration: " + e.getMessage());
            e.printStackTrace();
            showMessage("An unexpected error occurred", true);
            NotificationManager.showError(window, "Registration Error", "An unexpected error occurred");
        }
    }

    /**
     * 处理登录按钮点击事件
     */
    @FXML
    private void handleLogin(ActionEvent event) {
        String username = emailPhone.getText().trim();
        String pwd = password.getText().trim();

        // Get the window for notifications
        Window window = emailPhone.getScene().getWindow();

        // 验证输入
        if (username.isEmpty() || pwd.isEmpty()) {
            showMessage("Please enter username and password", true);
            NotificationManager.showError(window, "Login Error", "Please enter username and password");
            return;
        }

        // Special handling for test account - completely offline mode
        if ("test".equals(username) && "test".equals(pwd)) {
            handleSuccessfulLogin(username, "mock-token-test-account", event);
            return;
        }

        try {
            // 调用登录API
            AuthService authService = apiServiceFactory.getAuthService();
            AuthResponse response = authService.login(username, pwd);

            // Handle successful login
            handleSuccessfulLogin(username, response.getToken(), event);
        } catch (ApiException e) {
            showMessage(e.getMessage(), true);
            NotificationManager.showError(window, "Login Error", e.getMessage());
        }
    }

    /**
     * Handle successful login and navigate to main application
     */
    private void handleSuccessfulLogin(String username, String token, ActionEvent event) {
        // 设置用户上下文
        apiServiceFactory.setUserContext(username, token);

        // 切换到基础视图（包含全局侧边栏）
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/BaseView.fxml"));
            Parent baseView = loader.load();

            // 获取控制器并设置用户名
            BaseViewController baseViewController = loader.getController();
            baseViewController.setUsername(username);
            baseViewController.setToken(token);

            // 切换到基础视图
            // Get current window size for the new scene
            Stage currentStage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            double width = currentStage.getWidth();
            double height = currentStage.getHeight();
            Scene scene = new Scene(baseView, width, height); // Use current window dimensions
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showMessage("Error loading application view", true);
            NotificationManager.showError(emailPhone.getScene().getWindow(), "Error", "Error loading application view");
        }
    }

    /**
     * 切换到注册界面
     */
    @FXML
    private void switchToRegister(ActionEvent event) {
        try {
            Parent registerView = FXMLLoader.load(getClass().getResource("/fxml/RegisterView.fxml"));
            // Get current window size for the new scene
            Stage currentStage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            double width = currentStage.getWidth();
            double height = currentStage.getHeight();
            Scene scene = new Scene(registerView, width, height); // Use current window dimensions
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
            // Get current window size for the new scene
            Stage currentStage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            double width = currentStage.getWidth();
            double height = currentStage.getHeight();
            Scene scene = new Scene(loginView, width, height); // Use current window dimensions
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
            Window window = emailPhone.getScene().getWindow();
            if (code.isEmpty()) {
                showMessage("Please enter a verification code", true);
                NotificationManager.showError(window, "Verification Error", "Please enter a verification code");
            } else {
                try {
                    // 调用验证码API
                    AuthService authService = apiServiceFactory.getAuthService();
                    authService.verifyCode("temp-user-id", code, "email");

                    showMessage("Verification code accepted", false);
                    NotificationManager.showSuccess(window, "Verification Successful", "Verification code accepted");
                } catch (ApiException e) {
                    showMessage(e.getMessage(), true);
                    NotificationManager.showError(window, "Verification Error", e.getMessage());
                }
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


}