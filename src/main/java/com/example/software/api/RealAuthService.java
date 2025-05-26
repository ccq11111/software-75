package com.example.software.api;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Real implementation of the authentication service
 */
public class RealAuthService implements AuthService {

    private static final String BASE_URL = "http://localhost:8080/api/purseai/v1";
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Override
    public AuthResponse login(String username, String password) throws ApiException {
        try {
            // Create the request body
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("username", username);
            requestBody.put("password", password);

            // Create the HTTP request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/auth/login"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
                    .build();

            // Send the request
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // Check if the request was successful
            if (response.statusCode() == 200) {
                // Parse the response
                return objectMapper.readValue(response.body(), AuthResponse.class);
            } else {
                // Parse the error response
                ApiError error = objectMapper.readValue(response.body(), ApiError.class);
                throw new ApiException(error, response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            throw new ApiException("Failed to login: " + e.getMessage(), e);
        }
    }

    @Override
    public RegistrationResponse register(String username, String password, String email, String phone) throws ApiException {
        try {
            // Create the request body
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("username", username);
            requestBody.put("password", password);
            if (email != null) requestBody.put("email", email);
            if (phone != null) requestBody.put("phone", phone);

            // Create the HTTP request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/auth/register"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
                    .build();

            // Send the request
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // Check if the request was successful
            if (response.statusCode() == 200) {
                // Parse the response
                return objectMapper.readValue(response.body(), RegistrationResponse.class);
            } else {
                // Parse the error response
                ApiError error = objectMapper.readValue(response.body(), ApiError.class);
                throw new ApiException(error, response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            throw new ApiException("Failed to register: " + e.getMessage(), e);
        }
    }

    @Override
    public ApiResponse verifyCode(String userId, String code, String type) throws ApiException {
        try {
            // Create the request body
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("userId", userId);
            requestBody.put("code", code);
            requestBody.put("type", type);

            // Create the HTTP request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/auth/verification"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
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
            throw new ApiException("Failed to verify code: " + e.getMessage(), e);
        }
    }

    @Override
    public AuthResponse refreshToken(String currentToken) throws ApiException {
        try {
            // 创建 HTTP 请求
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/auth/refresh"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + currentToken)
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();

            // 发送请求
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // 检查请求是否成功
            if (response.statusCode() == 200) {
                // 解析响应
                return objectMapper.readValue(response.body(), AuthResponse.class);
            } else {
                // 解析错误响应
                ApiError error = objectMapper.readValue(response.body(), ApiError.class);
                throw new ApiException(error, response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            throw new ApiException("Failed to refresh token: " + e.getMessage(), e);
        }
    }
}
