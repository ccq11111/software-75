package org.merak.aidemo.service;

import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatClient;
import org.springframework.stereotype.Service;

@Service
public class AiService {
    private final OllamaChatClient chatClient;

    public AiService(OllamaChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public String chat(String message) {
        ChatResponse response = chatClient.call(new Prompt(message));
        return response.getResult().getOutput().getContent();
    }
} 