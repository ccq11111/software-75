package com.example.loginapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.merak.aidemo.AiDemoApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.net.URL;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class Launcher extends Application {
    private static ConfigurableApplicationContext springContext;
    private static final int MAX_RETRIES = 10;
    private static final int RETRY_DELAY_MS = 1000;
    private static final CountDownLatch contextLatch = new CountDownLatch(1);

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            // 等待 Spring Boot 启动
            int retries = 0;
            while (springContext == null && retries < MAX_RETRIES) {
                System.out.println("Attempting to get Spring context... (" + (retries + 1) + "/" + MAX_RETRIES + ")");
                springContext = AiDemoApplication.getContext();
                
                if (springContext == null) {
                    System.out.println("Spring context not available yet, waiting...");
                    Thread.sleep(RETRY_DELAY_MS);
                    retries++;
                } else {
                    System.out.println("Spring context successfully obtained!");
                    break;
                }
            }

            if (springContext == null) {
                System.err.println("Failed to get Spring context after " + MAX_RETRIES + " attempts");
                throw new IllegalStateException("Spring context is not initialized after " + MAX_RETRIES + " retries. Please start the Spring Boot application first.");
            }
            
            // 获取 FXML 文件的 URL
            URL fxmlUrl = getClass().getResource("/fxml/AIView.fxml");
            if (fxmlUrl == null) {
                System.err.println("Cannot find FXML file: /fxml/AIView.fxml");
                throw new IllegalStateException("Cannot find FXML file: /fxml/AIView.fxml");
            }
            
            System.out.println("Loading FXML from: " + fxmlUrl);
            
            // 使用 Spring 的 FXMLLoader
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            loader.setControllerFactory(springContext::getBean);
            
            Parent root = loader.load();
            Scene scene = new Scene(root);
            
            primaryStage.setTitle("AI Demo");
            primaryStage.setScene(scene);
            primaryStage.show();
            
            System.out.println("JavaFX application started successfully!");
        } catch (Exception e) {
            System.err.println("Error starting application: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public void stop() {
        // 注意：我们不再关闭 Spring 上下文，因为它是由独立的 Spring Boot 应用管理的
        System.out.println("JavaFX application stopped");
    }

    public static void main(String[] args) {
        // 直接启动 JavaFX 应用
        AiDemoApplication.main(args);
        launch(args);
    }
} 