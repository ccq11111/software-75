package com.example.saving;

import com.example.saving.controller.SummaryViewController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

@SpringBootApplication
public class SavingApplication extends Application {
    private static ConfigurableApplicationContext springContext;
    private Parent rootNode;

    public static void main(String[] args) {
        // 启动Spring Boot应用
        springContext = SpringApplication.run(SavingApplication.class, args);
        // 启动JavaFX应用
        launch(args);
    }

    @Override
    public void init() throws Exception {
        // 使用Spring的FXMLLoader来加载FXML文件
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/SummaryView.fxml"));
        fxmlLoader.setControllerFactory(springContext::getBean);
        rootNode = fxmlLoader.load();

        // 获取控制器
        SummaryViewController controller = fxmlLoader.getController();
        
        // 从后端获取token
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(
            "http://localhost:8089/v1/savings/token",
            HttpMethod.POST,
            new HttpEntity<>(new HttpHeaders()),
            String.class
        );
        
        // 使用ObjectMapper解析JSON响应
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> responseBody = mapper.readValue(response.getBody(), 
            new TypeReference<Map<String, String>>() {});
        
        // 验证token是否有效
        if (response.getStatusCode().is2xxSuccessful() && responseBody != null) {
            String token = responseBody.get("token");
            if (token != null && !token.isEmpty()) {
                controller.setAuthToken(token);
            } else {
                throw new RuntimeException("Failed to get valid token from backend");
            }
        } else {
            throw new RuntimeException("Failed to get token from backend");
        }
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Saving Application");
        Scene scene = new Scene(rootNode, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/styles/billing.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() {
        springContext.close();
    }
} 