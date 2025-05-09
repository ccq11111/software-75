package org.merak.aidemo.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
public class AiServiceTest {

    @Autowired
    private AiService aiService;

    @Test
    public void testChat() {
        String response = aiService.chat("Hello");
        assertNotNull(response, "Response should not be null");
    }
} 