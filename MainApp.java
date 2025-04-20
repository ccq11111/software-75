package com.example.loginapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;

/**
 * JavaFX应用程序主类
 */
public class MainApp extends Application {

    // Default window dimensions
    public static final double DEFAULT_WIDTH = 1000;
    public static final double DEFAULT_HEIGHT = 600;
    public static final double MIN_WIDTH = 800;
    public static final double MIN_HEIGHT = 500;

    @Override
    public void start(Stage primaryStage) throws Exception {
        // 加载登录界面FXML文件
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
        Parent root = loader.load();

        // 设置场景和窗口标题
        Scene scene = new Scene(root, DEFAULT_WIDTH, DEFAULT_HEIGHT);
        primaryStage.setTitle("PurseAI");
        primaryStage.setScene(scene);

        // Enable resizing
        primaryStage.setResizable(true);

        // Set minimum window size
        primaryStage.setMinWidth(MIN_WIDTH);
        primaryStage.setMinHeight(MIN_HEIGHT);

        // Center the window on screen
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        primaryStage.setX((screenBounds.getWidth() - DEFAULT_WIDTH) / 2);
        primaryStage.setY((screenBounds.getHeight() - DEFAULT_HEIGHT) / 2);

        // Add window resize listener
        primaryStage.widthProperty().addListener((obs, oldVal, newVal) -> {
            ResizeHelper.handleWindowResize(scene, newVal.doubleValue(), primaryStage.getHeight());
        });

        primaryStage.heightProperty().addListener((obs, oldVal, newVal) -> {
            ResizeHelper.handleWindowResize(scene, primaryStage.getWidth(), newVal.doubleValue());
        });

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}