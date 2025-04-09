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
    private TextField registerUsername;
    
    @FXML
    private PasswordField registerPassword;
    
    @FXML
    private PasswordField registerConfirmPassword;
    
    @FXML
    private TextField registerEmail;
    
    @FXML
    private Button registerButton;
    
    @FXML
    private Label registerMessage;

    // 登录界面组件
    @FXML
    private TextField loginUsername;
    
    @FXML
    private PasswordField loginPassword;
    
    @FXML
    private Button loginButton;
    
    @FXML
    private Label loginMessage;

    /**
     * 初始化控制器
     * 添加一些测试账户
     */
    @FXML
    public void initialize() {
        // 添加一个测试账户（如果还没有添加）
        if (userAccounts.isEmpty()) {
            userAccounts.put("admin", new UserAccount("admin", "admin123", "admin@example.com"));
        }
        
        // 初始化消息标签（仅当标签存在时）
        if (registerMessage != null) {
            registerMessage.setText("");
        }
        if (loginMessage != null) {
            loginMessage.setText("");
        }
    }

    /**
     * 处理注册按钮点击事件
     */
    @FXML
    private void handleRegister(ActionEvent event) {
        String username = registerUsername.getText().trim();
        String password = registerPassword.getText().trim();
        String confirmPassword = registerConfirmPassword.getText().trim();
        String email = registerEmail.getText().trim();
        
        // 验证输入
        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || email.isEmpty()) {
            showRegisterMessage("所有字段都必须填写", true);
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            showRegisterMessage("两次输入的密码不一致", true);
            return;
        }
        
        if (userAccounts.containsKey(username)) {
            showRegisterMessage("用户名已被占用", true);
            return;
        }
        
        // 创建新账户
        userAccounts.put(username, new UserAccount(username, password, email));
        showRegisterMessage("注册成功！您现在可以登录", false);
        clearRegisterFields();
        
        // 延迟切换到登录界面
        javafx.application.Platform.runLater(() -> {
            navigateToLogin(event);
        });
    }

    /**
     * 处理登录按钮点击事件
     */
    @FXML
    private void handleLogin(ActionEvent event) {
        String username = loginUsername.getText().trim();
        String password = loginPassword.getText().trim();
        
        // 验证输入
        if (username.isEmpty() || password.isEmpty()) {
            showLoginMessage("用户名和密码不能为空", true);
            return;
        }
        
        UserAccount account = userAccounts.get(username);
        
        if (account == null) {
            showLoginMessage("用户不存在", true);
            return;
        }
        
        if (!account.getPassword().equals(password)) {
            showLoginMessage("密码错误", true);
            return;
        }
        
        showLoginMessage("登录成功！欢迎回来，" + username, false);
        clearLoginFields();
    }
    
    /**
     * 切换到登录界面
     */
    @FXML
    private void navigateToLogin(ActionEvent event) {
        try {
            Parent loginView = FXMLLoader.load(getClass().getResource("/fxml/LoginView.fxml"));
            Scene scene = new Scene(loginView);
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 切换到注册界面
     */
    @FXML
    private void navigateToRegister(ActionEvent event) {
        try {
            Parent registerView = FXMLLoader.load(getClass().getResource("/fxml/RegisterView.fxml"));
            Scene scene = new Scene(registerView);
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Handle the Sign up link click to switch to register view
     */
    @FXML
    private void switchToRegister(ActionEvent event) {
        navigateToRegister(event);
    }
    
    /**
     * 显示注册消息
     */
    private void showRegisterMessage(String message, boolean isError) {
        if (registerMessage != null) {
            registerMessage.setText(message);
            registerMessage.setTextFill(isError ? Color.RED : Color.GREEN);
        }
    }
    
    /**
     * 显示登录消息
     */
    private void showLoginMessage(String message, boolean isError) {
        if (loginMessage != null) {
            loginMessage.setText(message);
            loginMessage.setTextFill(isError ? Color.RED : Color.WHITE);
        }
    }
    
    /**
     * 清空注册字段
     */
    private void clearRegisterFields() {
        if (registerUsername != null) {
            registerUsername.clear();
            registerPassword.clear();
            registerConfirmPassword.clear();
            registerEmail.clear();
        }
    }
    
    /**
     * 清空登录字段
     */
    private void clearLoginFields() {
        if (loginUsername != null) {
            loginUsername.clear();
            loginPassword.clear();
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