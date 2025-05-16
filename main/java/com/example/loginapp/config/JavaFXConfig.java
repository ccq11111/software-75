package com.example.loginapp.config;

import com.example.loginapp.LoginRegisterController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JavaFXConfig {
    
    @Bean
    public LoginRegisterController loginRegisterController() {
        return new LoginRegisterController();
    }
} 