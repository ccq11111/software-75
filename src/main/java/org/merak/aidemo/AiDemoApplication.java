package org.merak.aidemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication(scanBasePackages = {"org.merak.aidemo", "com.example.loginapp"})
public class AiDemoApplication {
    private static ConfigurableApplicationContext context;
    private static final Object contextLock = new Object();

    public static void main(String[] args) {
        // 启动 Spring Boot 应用
        synchronized (contextLock) {
            context = SpringApplication.run(AiDemoApplication.class, args);
            System.out.println("Spring Boot application started with context: " + context);
        }
    }

    public static ConfigurableApplicationContext getContext() {
        synchronized (contextLock) {
            if (context == null) {
                System.out.println("Spring context is not initialized yet");
                return null;
            }
            System.out.println("Returning existing Spring context: " + context);
            return context;
        }
    }
} 