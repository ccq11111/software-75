package com.example.loginapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.example.loginapp")
public class SavingSpringBootApp {
    public static void main(String[] args) {
        SpringApplication.run(SavingSpringBootApp.class, args);
    }
}
