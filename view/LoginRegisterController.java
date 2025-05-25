package com.example.software.view;

import com.example.software.api.*;
import com.example.software.security.TokenManager;
import com.example.software.util.NotificationManager;
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
    private final ApiServiceFactory apiServiceFactory = ApiServiceFactory.getInstance();// 获取API服务工厂实例

    // 注册界面组件
    @FXML
    private TextField emailPhone;// 用户输入的邮箱或手机号字段

    @FXML
    private PasswordField password; // 密码字段

    @FXML
    private PasswordField confirmPassword;// 注册时确认密码字段

    @FXML
    private Label messageLabel;// 显示消息的标签（错误或成功信息）

    @FXML
    private Hyperlink verificationLink; // 验证码链接（用于登录时）

    /**
     * 初始化控制器
     */
    @FXML
    public void initialize() {
        // Add event handler for verification link if it exists (it's only in the login view)
        // 如果verificationLink存在，则添加事件处理器（此链接只在登录界面中存在）
        if (verificationLink != null) {
            verificationLink.setOnAction(this::handleVerificationCode);
        }
    }

    /**
     * 处理注册按钮点击事件
     */
    @FXML
    private void handleRegister(ActionEvent event) {
        String username = emailPhone.getText().trim();// 获取输入的用户名（邮箱或手机号）
        String pwd = password.getText().trim(); // 获取输入的密码
        String confirmPwd = confirmPassword.getText().trim(); // 获取确认密码

        // Get the window for notifications
        Window window = emailPhone.getScene().getWindow();// 获取当前窗口引用，用于弹出通知

        // 验证输入
        // 验证输入字段是否为空
        if (username.isEmpty() || pwd.isEmpty() || confirmPwd.isEmpty()) {
            showMessage("All fields must be filled", true); // 显示错误消息
            NotificationManager.showError(window, "Registration Error", "All fields must be filled"); // 弹出错误通知
            return;
        }

        // 检查密码和确认密码是否一致
        if (!pwd.equals(confirmPwd)) {
            showMessage("Passwords do not match", true); // 显示错误消息
            NotificationManager.showError(window, "Registration Error", "Passwords do not match"); // 弹出错误通知
            return;
        }

        try {
            // 调用注册API
            AuthService authService = apiServiceFactory.getAuthService();
            RegistrationResponse response = authService.register(username, pwd, "", "");

            // 判断注册是否成功
            if (response.isSuccess()) {
                showMessage("Registration successful!", false); // 显示成功消息
                NotificationManager.showSuccess(window, "Registration Successful", "Your account has been created successfully!"); // 弹出成功通知
                clearFields(); // 清空输入字段
                // 切换到登录界面
                navigateToLogin(event);
            } else {
                showMessage(response.getMessage(), true); // 显示错误消息
                NotificationManager.showError(window, "Registration Error", response.getMessage()); // 弹出错误通知
            }
        }catch (ApiException e) {
            System.err.println("Registration error: " + e.getMessage());
            e.printStackTrace();
            showMessage(e.getMessage(), true); // 显示错误消息
            NotificationManager.showError(window, "Registration Error", e.getMessage()); // 弹出错误通知
        } catch (Exception e) {
            System.err.println("Unexpected error during registration: " + e.getMessage());
            e.printStackTrace();
            showMessage("An unexpected error occurred", true); // 显示通用错误消息
            NotificationManager.showError(window, "Registration Error", "An unexpected error occurred"); // 弹出错误通知
        }
    }

    /**
     * 处理登录按钮点击事件
     */
    @FXML
    private void handleLogin(ActionEvent event) {
        String username = emailPhone.getText().trim();// 获取输入的用户名（邮箱或手机号）
        String pwd = password.getText().trim(); // 获取输入的密码

        // Get the window for notifications
        Window window = emailPhone.getScene().getWindow();// 获取当前窗口引用，用于弹出通知

        // 验证输入字段是否为空
        if (username.isEmpty() || pwd.isEmpty()) {
            showMessage("Please enter username and password", true); // 显示错误消息
            NotificationManager.showError(window, "Login Error", "Please enter username and password"); // 弹出错误通知
            return;
        }

        // 测试账户特殊处理 - 离线模式
        if ("test".equals(username) && "test".equals(pwd)) {
            handleSuccessfulLogin(username, "mock-token-test-account", event); // 模拟登录成功
            return;
        }

        try {
            // 调用登录API
            AuthService authService = apiServiceFactory.getAuthService();
            AuthResponse response = authService.login(username, pwd);

            // 确保检查响应的success字段
            if (!response.isSuccess()) {
                showMessage(response.getMessage(), true);
                NotificationManager.showError(window, "Login Error", response.getMessage());
                return;
            }

            // 登录成功，设置用户上下文并切换界面
            apiServiceFactory.setUserContext(username, response.getToken());
            handleSuccessfulLogin(username, response.getToken(), event);
        } catch (ApiException e) {
            showMessage(e.getMessage(), true);
            NotificationManager.showError(window, "Login Error", e.getMessage());
        } catch (Exception e) {
            showMessage("An unexpected error occurred", true);
            NotificationManager.showError(window, "Login Error", "An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle successful login and navigate to main application
     */
    private void handleSuccessfulLogin(String username, String token, ActionEvent event) {
        // 设置 token 管理器
        TokenManager.getInstance().setToken(username, token, 86400); // 假设 token 有效期为 24 小时

        // 设置用户上下文
        apiServiceFactory.setUserContext(username, token);

        // 切换到基础视图（包含全局侧边栏）
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/BaseView.fxml"));
            Parent baseView = loader.load();

            // 获取控制器并设置用户名
            BaseViewController baseViewController = loader.getController();
            baseViewController.setUsername(username);

            // 切换到基础视图
            // 获取当前窗口尺寸
            Stage currentStage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            double width = currentStage.getWidth();
            double height = currentStage.getHeight();
            Scene scene = new Scene(baseView, width, height);
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
            // Get current window size for the new scene获取当前窗口尺寸
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
            // Get current window size for the new scene 获取当前窗口尺寸
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
                showMessage("Please enter a verification code", true);// 显示错误消息
                NotificationManager.showError(window, "Verification Error", "Please enter a verification code");
            } else {
                try {
                    // 调用验证码API
                    AuthService authService = apiServiceFactory.getAuthService();
                    authService.verifyCode("temp-user-id", code, "email");

                    showMessage("Verification code accepted", false);// 显示成功消息
                    NotificationManager.showSuccess(window, "Verification Successful", "Verification code accepted");
                } catch (ApiException e) {
                    showMessage(e.getMessage(), true);
                    NotificationManager.showError(window, "Verification Error", e.getMessage()); // 弹出错误通知
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
            messageLabel.setTextFill(isError ? Color.RED : Color.GREEN);// 设置文本颜色，错误时为红色，成功时为绿色
        }
    }

    /**
     * 清空字段
     */
    private void clearFields() {
        emailPhone.clear(); // 清空邮箱/手机号字段
        password.clear(); // 清空密码字段
        if (confirmPassword != null) {
            confirmPassword.clear(); // 清空确认密码字段
        }
    }


}