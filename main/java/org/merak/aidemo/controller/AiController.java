package com.example.software.controller;

import com.example.software.service.AiService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

@RestController
@RequestMapping("/v1/ai")
public class AiController {
    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

//    @PostMapping("/chat")
//    public String chat(@RequestBody String message) {
//        return aiService.chat(message);
//    }
    @PostMapping("/chat")
    public String chat(@RequestBody Map<String, String> body) {
        String message = body.get("message");
        String chat = aiService.chat(message);
        return chat;
    }

    @PostMapping("/holiday-advice")
    public String holidayAdvice(@RequestBody Map<String, String> body) {
        String csvPath = body.getOrDefault("csvPath", "data/billing/billingEntries.csv");
        return aiService.holidaySpendingAdvice(csvPath);
    }

    @PostMapping("/tourism-advice")
    public String tourismAdvice(@RequestBody Map<String, String> body) {
        String city = body.getOrDefault("city", "");
        return aiService.tourismAdvice(city);
    }

    @GetMapping("/consume-analysis")
    public String consumeAnalysis() {
        return aiService.consumeAnalysis();
    }

    @PostMapping("/consume-record")
    public String consumeRecord(@RequestBody Map<String, String> body) {
        String record = body.get("record");
        return aiService.consumeRecordAndAdd(record);
    }

    @GetMapping("/periodic-reminders")
    public String periodicReminders() {
        return aiService.periodicReminders();
    }

}