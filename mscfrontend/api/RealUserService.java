package com.example.loginapp.api;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Real implementation of the user service
 */
public class RealUserService implements UserService {

    private static final String BASE_URL = "http://127.0.0.1:8080/v1";
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final String token;

    public RealUserService(String token) {
        this.token = token;
    }

    @Override
    public UserSettings getUserSettings() throws ApiException {
        try {
            // Create the HTTP request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/users/settings"))
                    .header("Authorization", "Bearer " + token)
                    .GET()
                    .build();

            // Send the request
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // Check if the request was successful
            if (response.statusCode() == 200) {
                // Parse the response
                return objectMapper.readValue(response.body(), UserSettings.class);
            } else {
                // Parse the error response
                ApiError error = objectMapper.readValue(response.body(), ApiError.class);
                throw new ApiException(error, response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            throw new ApiException("Failed to get user settings: " + e.getMessage(), e);
        }
    }

    @Override
    public UserSettings updateUserSettings(String email, String phone, String currency,
                                          String language, Boolean emailNotifications,
                                          Boolean pushNotifications) throws ApiException {
        try {
            // Create the request body
            Map<String, Object> requestBody = new HashMap<>();
            if (email != null) requestBody.put("email", email);
            if (phone != null) requestBody.put("phone", phone);
            if (currency != null) requestBody.put("currency", currency);
            if (language != null) requestBody.put("language", language);

            // Add notifications if any are provided
            if (emailNotifications != null || pushNotifications != null) {
                Map<String, Boolean> notifications = new HashMap<>();
                if (emailNotifications != null) notifications.put("email", emailNotifications);
                if (pushNotifications != null) notifications.put("push", pushNotifications);
                requestBody.put("notifications", notifications);
            }

            // Create the HTTP request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/users/settings"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .PUT(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
                    .build();

            // Send the request
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // Check if the request was successful
            if (response.statusCode() == 200) {
                // Parse the response
                return objectMapper.readValue(response.body(), UserSettings.class);
            } else {
                // Parse the error response
                ApiError error = objectMapper.readValue(response.body(), ApiError.class);
                throw new ApiException(error, response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            throw new ApiException("Failed to update user settings: " + e.getMessage(), e);
        }
    }
}
