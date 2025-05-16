package com.example.loginapp;

import org.merak.aidemo.AiSpringBootApp;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Launcher extends Application {
    private static ConfigurableApplicationContext savingContext;
    private static ConfigurableApplicationContext aiContext;

    @Override
    public void init() throws Exception {
        // 启动 Spring Boot 后端服务（AI 和 Saving）
        Thread savingThread = new Thread(() -> {
            try {
                savingContext = SpringApplication.run(SavingSpringBootApp.class);
            } catch (Exception e) {
                System.err.println("Failed to start Saving service: " + e.getMessage());
                e.printStackTrace();
            }
        });

        Thread aiThread = new Thread(() -> {
            try {
                aiContext = SpringApplication.run(AiSpringBootApp.class);
            } catch (Exception e) {
                System.err.println("Failed to start AI service: " + e.getMessage());
                e.printStackTrace();
            }
        });

        savingThread.setDaemon(true);
        aiThread.setDaemon(true);

        savingThread.start();
        aiThread.start();

        // 等待服务启动
        int maxWaitTime = 10; // 最大等待时间（秒）
        int currentWait = 0;
        while ((savingContext == null || aiContext == null) && currentWait < maxWaitTime) {
            Thread.sleep(1000);
            currentWait++;
        }

        if (savingContext == null || aiContext == null) {
            throw new RuntimeException("Failed to start one or more services within " + maxWaitTime + " seconds");
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        if (savingContext == null) {
            System.err.println("Saving backend failed to start");
            return;
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/BaseView.fxml"));
        loader.setControllerFactory(clazz -> savingContext.getBean(clazz));
        Parent root = loader.load();

        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle("Unified AI & Saving App");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void stop() throws Exception {
        if (savingContext != null) savingContext.close();
        if (aiContext != null) aiContext.close();
    }
}
