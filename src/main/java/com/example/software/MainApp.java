package com.example.software;

import com.example.software.util.ResizeHelper;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.springframework.boot.autoconfigure.SpringBootApplication;

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

        // 监听窗口关闭
        primaryStage.setOnCloseRequest(event -> {
            SoftwareApplication.context.close();      // 关闭 Spring Boot 容器
            Platform.exit();      // 退出 JavaFX
            System.exit(0);       // 强制退出 JVM（确保所有线程退出）
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}