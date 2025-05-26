package com.example.software;

import javafx.application.Platform;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class SoftwareApplication {
	public static ConfigurableApplicationContext context;
	public static void main(String[] args) {
		// 启动时保存 Spring 上下文
		context = SpringApplication.run(SoftwareApplication.class, args);
		MainApp.main(args);
	}
}
