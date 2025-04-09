package com.example.loginapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * JavaFX应用程序主类
 */
public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // 加载登录界面FXML文件
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
        Parent root = loader.load();

        // 设置场景和窗口标题
        Scene scene = new Scene(root, 1000, 600);
        primaryStage.setTitle("PurseAI");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}