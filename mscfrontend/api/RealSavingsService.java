package com.example.loginapp.api;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Real implementation of the savings service
 */
public class RealSavingsService implements SavingsService {

    private static final String BASE_URL = "http://127.0.0.1:8080/v1";
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private final String token;

    public RealSavingsService(String token) {
        this.token = token;
    }

    @Override
    public SavingPlanResponse createPlan(String name, LocalDate startDate, String cycle,
                                        int cycleTimes, BigDecimal amount, String currency) throws ApiException {
        try {
            // Create the request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("name", name);
            requestBody.put("startDate", startDate.toString());
            requestBody.put("cycle", cycle);
            requestBody.put("cycleTimes", cycleTimes);
            requestBody.put("amount", amount);
            requestBody.put("currency", currency);

            // Create the HTTP request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/savings/plans"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
                    .build();

            // Send the request
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // Check if the request was successful
            if (response.statusCode() == 200) {
                // Parse the response
                return objectMapper.readValue(response.body(), SavingPlanResponse.class);
            } else {
                // Parse the error response
                ApiError error = objectMapper.readValue(response.body(), ApiError.class);
                throw new ApiException(error, response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            throw new ApiException("Failed to create saving plan: " + e.getMessage(), e);
        }
    }

    @Override
    public List<SavingPlan> getAllPlans() throws ApiException {
        try {
            // Create the HTTP request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/savings/plans"))
                    .header("Authorization", "Bearer " + token)
                    .GET()
                    .build();

            // Send the request
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // Check if the request was successful
            if (response.statusCode() == 200) {
                // Parse the response
                Map<String, Object> responseMap = objectMapper.readValue(response.body(), new TypeReference<Map<String, Object>>() {});
                List<SavingPlan> plans = objectMapper.convertValue(responseMap.get("plans"), new TypeReference<List<SavingPlan>>() {});
                return plans;
            } else {
                // Parse the error response
                ApiError error = objectMapper.readValue(response.body(), ApiError.class);
                throw new ApiException(error, response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            throw new ApiException("Failed to get saving plans: " + e.getMessage(), e);
        }
    }

    @Override
    public SavingPlanResponse updatePlan(String planId, String name, LocalDate startDate,
                                        String cycle, int cycleTimes, BigDecimal amount,
                                        String currency, BigDecimal savedAmount) throws ApiException {
        try {
            // Create the request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("name", name);
            requestBody.put("startDate", startDate.toString());
            requestBody.put("cycle", cycle);
            requestBody.put("cycleTimes", cycleTimes);
            requestBody.put("amount", amount);
            requestBody.put("currency", currency);
            requestBody.put("savedAmount", savedAmount);

            // Create the HTTP request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/savings/plans/" + planId))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .PUT(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
                    .build();

            // Send the request
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // Check if the request was successful
            if (response.statusCode() == 200) {
                // Parse the response
                return objectMapper.readValue(response.body(), SavingPlanResponse.class);
            } else {
                // Parse the error response
                ApiError error = objectMapper.readValue(response.body(), ApiError.class);
                throw new ApiException(error, response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            throw new ApiException("Failed to update saving plan: " + e.getMessage(), e);
        }
    }

    @Override
    public ApiResponse deletePlan(String planId) throws ApiException {
        try {
            // Create the HTTP request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/savings/plans/" + planId))
                    .header("Authorization", "Bearer " + token)
                    .DELETE()
                    .build();

            // Send the request
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // Check if the request was successful
            if (response.statusCode() == 200) {
                // Parse the response
                return objectMapper.readValue(response.body(), ApiResponse.class);
            } else {
                // Parse the error response
                ApiError error = objectMapper.readValue(response.body(), ApiError.class);
                throw new ApiException(error, response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            throw new ApiException("Failed to delete saving plan: " + e.getMessage(), e);
        }
    }
}
