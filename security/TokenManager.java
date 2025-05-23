package com.example.software.security;

import com.example.software.api.ApiException;
import com.example.software.api.ApiServiceFactory;
import com.example.software.api.AuthResponse;
import com.example.software.api.AuthService;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 管理 JWT token 的生命周期、存储和刷新
 */
public class TokenManager {
    private static TokenManager instance;
    private String token;
    private String username;
    private Instant expirationTime;
    private final ScheduledExecutorService scheduler;
    private static final long REFRESH_BUFFER_SECONDS = 300; // 在过期前5分钟刷新

    private TokenManager() {
        this.scheduler = Executors.newScheduledThreadPool(1);
    }

    public static synchronized TokenManager getInstance() {
        if (instance == null) {
            instance = new TokenManager();
        }
        return instance;
    }

    public void setToken(String username, String token, long expiresIn) {
        this.username = username;
        this.token = token;
        this.expirationTime = Instant.now().plusSeconds(expiresIn);
        
        // 安排token刷新
        scheduleTokenRefresh(expiresIn);
    }

    public String getToken() {
        return token;
    }

    public boolean isTokenValid() {
        return token != null && Instant.now().isBefore(expirationTime);
    }

    private void scheduleTokenRefresh(long expiresIn) {
        // 在token过期前5分钟尝试刷新
        long refreshDelay = expiresIn - REFRESH_BUFFER_SECONDS;
        if (refreshDelay > 0) {
            scheduler.schedule(this::refreshToken, refreshDelay, TimeUnit.SECONDS);
        }
    }

    private void refreshToken() {
        try {
            AuthService authService = ApiServiceFactory.getInstance().getAuthService();
            // 这里假设后端提供了刷新token的接口
            AuthResponse response = authService.refreshToken(token);
            setToken(username, response.getToken(), response.getExpiresIn());
        } catch (ApiException e) {
            // 如果刷新失败，重定向到登录页面
            Platform.runLater(this::redirectToLogin);
        }
    }

    public void clearToken() {
        this.token = null;
        this.username = null;
        this.expirationTime = null;
    }

    private void redirectToLogin() {
        try {
            // 清除当前token
            clearToken();
            ApiServiceFactory.getInstance().clearUserContext();

            // 加载登录页面
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginRegister.fxml"));
            Parent loginView = loader.load();
            Scene scene = new Scene(loginView);
            
            // 获取当前活动窗口并切换场景
            Stage stage = getActiveStage();
            if (stage != null) {
                stage.setScene(scene);
                stage.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Stage getActiveStage() {
        // 获取当前活动窗口
        return javafx.stage.Window.getWindows().stream()
                .filter(window -> window instanceof Stage && window.isShowing())
                .map(window -> (Stage) window)
                .findFirst()
                .orElse(null);
    }

    public void shutdown() {
        scheduler.shutdown();
    }
} 