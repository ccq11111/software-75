package org.merak.aidemo.controller;

import org.merak.aidemo.service.AiService;
import org.merak.aidemo.service.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/ai")
public class AiController {
    private final AiService aiService;
    private final JwtService jwtService;

    public AiController(AiService aiService, JwtService jwtService) {
        this.aiService = aiService;
        this.jwtService = jwtService;
    }

    @PostMapping("/chat")
    public String chat(@RequestBody String message, @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or missing authentication token");
        }
        
        String token = authHeader.substring(7); // Remove "Bearer " prefix
        if (!jwtService.validateToken(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }
        
        return aiService.chat(message);
    }
} 