package com.example.software.util;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Window;

/**
 * 统一管理应用程序的通知显示
 */
public class NotificationManager {
    
    /**
     * 显示错误通知
     *
     * @param owner 父窗口
     * @param title 标题
     * @param message 错误消息
     */
    public static void showError(Window owner, String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(owner);
        alert.showAndWait();
    }
    
    /**
     * 显示成功通知
     *
     * @param owner 父窗口
     * @param title 标题
     * @param message 成功消息
     */
    public static void showSuccess(Window owner, String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(owner);
        alert.showAndWait();
    }
    
    /**
     * 显示警告通知
     *
     * @param owner 父窗口
     * @param title 标题
     * @param message 警告消息
     */
    public static void showWarning(Window owner, String title, String message) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(owner);
        alert.showAndWait();
    }
} 