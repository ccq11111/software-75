package com.example.software.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Real implementation of the AI service
 */
public class RealAIService implements AIService {

    private static final String BASE_URL = "http://localhost:8080/api/purseai/v1";
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final String token;

    public RealAIService(String token) {
        this.token = token;
    }

    @Override
    public AIAdviceResponse getAdvice(String message, boolean includeTransactions,
                                     boolean includeSavings, boolean includeSummary) throws ApiException {
        try {
            // Create the request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("message", message);

            Map<String, Boolean> context = new HashMap<>();
            context.put("includeTransactions", includeTransactions);
            context.put("includeSavings", includeSavings);
            context.put("includeSummary", includeSummary);
//            requestBody.put("context", context);

            // Create the HTTP request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/ai/chat"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
                    .build();

            // Send the request
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // Check if the request was successful
            if (response.statusCode() == 200) {
                // Parse the response
                String body = response.body();
                final String processedResponse = body
                        .replaceAll("(?s)<think>.*?</think>", "") // 使用(?s)使.能匹配换行符
                        .replaceAll("(?s)<think>.*</think>", "")  // 处理没有闭合标签的情况
                        .replaceAll("```.*?```", "")
                        .replaceAll("`.*?`", "")
                        .replaceAll("\\*\\*.*?\\*\\*", "")
                        .replaceAll("\\*.*?\\*", "")
                        .replaceAll("#{1,6}\\s", "")
                        .replaceAll("\\[.*?\\]\\(.*?\\)", "")
                        .replaceAll(">\\s", "")
                        .replaceAll("-\\s", "")
                        .replaceAll("\\d+\\.\\s", "")
                        .trim();
                AIAdviceResponse aiAdviceResponse = new AIAdviceResponse();
                aiAdviceResponse.setMessage(processedResponse);
                return aiAdviceResponse;
                //return objectMapper.readValue(response.body(), AIAdviceResponse.class);
            } else {
                // Parse the error response
                ApiError error = objectMapper.readValue(response.body(), ApiError.class);
                throw new ApiException(error, response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            throw new ApiException("Failed to get AI advice: " + e.getMessage(), e);
        }
    }

    @Override
    public List<QuickAction> getQuickActions() throws ApiException {
        try {
            // Create the HTTP request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/ai/quick-actions"))
                    .header("Authorization", "Bearer " + token)
                    .GET()
                    .build();

            // Send the request
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // Check if the request was successful
            if (response.statusCode() == 200) {
                // Parse the response
                Map<String, Object> responseMap = objectMapper.readValue(response.body(), new TypeReference<Map<String, Object>>() {});
                List<QuickAction> actions = objectMapper.convertValue(responseMap.get("actions"), new TypeReference<List<QuickAction>>() {});
                return actions;
            } else {
                // Parse the error response
                ApiError error = objectMapper.readValue(response.body(), ApiError.class);
                throw new ApiException(error, response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            throw new ApiException("Failed to get quick actions: " + e.getMessage(), e);
        }
    }

    @Override
    public AIAdviceResponse getHolidayAdvice(String csvPath) throws ApiException {
        try {
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("csvPath", csvPath);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/ai/holiday-advice"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                String body = response.body();
                AIAdviceResponse aiAdviceResponse = new AIAdviceResponse();
                aiAdviceResponse.setMessage(body);
                return aiAdviceResponse;
            } else {
                ApiError error = objectMapper.readValue(response.body(), ApiError.class);
                throw new ApiException(error, response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            throw new ApiException("Failed to get holiday advice: " + e.getMessage(), e);
        }
    }

    @Override
    public AIAdviceResponse getTourismAdvice(String city) throws ApiException {
        try {
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("city", city);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/ai/tourism-advice"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                String body = response.body();
                AIAdviceResponse aiAdviceResponse = new AIAdviceResponse();
                aiAdviceResponse.setMessage(body);
                return aiAdviceResponse;
            } else {
                ApiError error = objectMapper.readValue(response.body(), ApiError.class);
                throw new ApiException(error, response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            throw new ApiException("Failed to get tourism advice: " + e.getMessage(), e);
        }
    }

    @Override
    public String get(String url) throws ApiException {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + url))
                    .header("Authorization", "Bearer " + token)
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return response.body();
            } else {
                throw new ApiException("GET请求失败: " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            throw new ApiException("GET请求异常: " + e.getMessage(), e);
        }
    }

    @Override
    public String postConsumeRecord(String record) throws ApiException {
        try {
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("record", record);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/ai/consume-record"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return response.body();
            } else {
                throw new ApiException("消费记录POST失败: " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            throw new ApiException("消费记录POST异常: " + e.getMessage(), e);
        }
    }

    @Override
    public String getPeriodicReminders() throws ApiException {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/ai/periodic-reminders"))
                    .header("Authorization", "Bearer " + token)
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return response.body();
            } else {
                throw new ApiException("获取周期性提醒失败: " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            throw new ApiException("获取周期性提醒异常: " + e.getMessage(), e);
        }
    }
}
