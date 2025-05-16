package org.merak.aidemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "org.merak.aidemo")
public class AiSpringBootApp {
    public static void main(String[] args) {
        SpringApplication.run(AiSpringBootApp.class, args);
    }
}
