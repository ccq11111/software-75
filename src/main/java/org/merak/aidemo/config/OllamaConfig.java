package org.merak.aidemo.config;

import org.springframework.ai.ollama.OllamaChatClient;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OllamaConfig {
    
    @Bean
    public OllamaApi ollamaApi() {
        return new OllamaApi();
    }
    
    @Bean
    public OllamaChatClient ollamaChatClient(OllamaApi ollamaApi) {
        return new OllamaChatClient(ollamaApi)
                .withDefaultOptions(OllamaOptions.create()
                        .withModel("deepseek-r1")
                        .withTemperature(0.8f));
    }
} 