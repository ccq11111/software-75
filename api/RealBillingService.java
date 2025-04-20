package com.example.loginapp.api;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Real implementation of the billing service
 */
public class RealBillingService implements BillingService {

    private static final String BASE_URL = "http://127.0.0.1:8080/v1";
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private final String token;

    public RealBillingService(String token) {
        this.token = token;
    }

    @Override
    public BillingEntryResponse createEntry(String category, String product, BigDecimal price,
                                           LocalDate date, LocalTime time, String remark) throws ApiException {
        try {
            // Create the request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("category", category);
            requestBody.put("product", product);
            requestBody.put("price", price);
            requestBody.put("date", date.toString());
            requestBody.put("time", time.toString());
            requestBody.put("remark", remark);

            // Create the HTTP request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/billing/entries"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
                    .build();

            // Send the request
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // Check if the request was successful
            if (response.statusCode() == 200) {
                // Parse the response
                return objectMapper.readValue(response.body(), BillingEntryResponse.class);
            } else {
                // Parse the error response
                ApiError error = objectMapper.readValue(response.body(), ApiError.class);
                throw new ApiException(error, response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            throw new ApiException("Failed to create billing entry: " + e.getMessage(), e);
        }
    }

    @Override
    public List<BillingEntry> getEntries(LocalDate startDate, LocalDate endDate,
                                        String category, String searchTerm) throws ApiException {
        try {
            // Build the query parameters
            StringBuilder queryParams = new StringBuilder();
            if (startDate != null) {
                queryParams.append(queryParams.length() == 0 ? "?" : "&").append("startDate=").append(startDate);
            }
            if (endDate != null) {
                queryParams.append(queryParams.length() == 0 ? "?" : "&").append("endDate=").append(endDate);
            }
            if (category != null && !category.isEmpty()) {
                queryParams.append(queryParams.length() == 0 ? "?" : "&").append("category=").append(category);
            }
            if (searchTerm != null && !searchTerm.isEmpty()) {
                queryParams.append(queryParams.length() == 0 ? "?" : "&").append("searchTerm=").append(searchTerm);
            }

            // Create the HTTP request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/billing/entries" + queryParams))
                    .header("Authorization", "Bearer " + token)
                    .GET()
                    .build();

            // Send the request
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // Check if the request was successful
            if (response.statusCode() == 200) {
                // Parse the response
                Map<String, Object> responseMap = objectMapper.readValue(response.body(), new TypeReference<Map<String, Object>>() {});
                List<BillingEntry> entries = objectMapper.convertValue(responseMap.get("entries"), new TypeReference<List<BillingEntry>>() {});
                return entries;
            } else {
                // Parse the error response
                ApiError error = objectMapper.readValue(response.body(), ApiError.class);
                throw new ApiException(error, response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            throw new ApiException("Failed to get billing entries: " + e.getMessage(), e);
        }
    }

    @Override
    public BillingEntryResponse updateEntry(String entryId, String category, String product,
                                          BigDecimal price, LocalDate date, LocalTime time,
                                          String remark) throws ApiException {
        try {
            // Create the request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("category", category);
            requestBody.put("product", product);
            requestBody.put("price", price);
            requestBody.put("date", date.toString());
            requestBody.put("time", time.toString());
            requestBody.put("remark", remark);

            // Create the HTTP request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/billing/entries/" + entryId))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .PUT(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
                    .build();

            // Send the request
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // Check if the request was successful
            if (response.statusCode() == 200) {
                // Parse the response
                return objectMapper.readValue(response.body(), BillingEntryResponse.class);
            } else {
                // Parse the error response
                ApiError error = objectMapper.readValue(response.body(), ApiError.class);
                throw new ApiException(error, response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            throw new ApiException("Failed to update billing entry: " + e.getMessage(), e);
        }
    }

    @Override
    public ApiResponse deleteEntry(String entryId) throws ApiException {
        try {
            // Create the HTTP request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/billing/entries/" + entryId))
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
            throw new ApiException("Failed to delete billing entry: " + e.getMessage(), e);
        }
    }

    @Override
    public ImportResponse importFromCsv(File file) throws ApiException {
        try {
            // Create the multipart request
            String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
            String lineEnd = "\r\n";
            String twoHyphens = "--";

            // Read the file content
            byte[] fileContent = Files.readAllBytes(file.toPath());

            // Create the multipart body
            StringBuilder requestBody = new StringBuilder();
            requestBody.append(twoHyphens).append(boundary).append(lineEnd);
            requestBody.append("Content-Disposition: form-data; name=\"file\"; filename=\"").append(file.getName()).append("\"").append(lineEnd);
            requestBody.append("Content-Type: text/csv").append(lineEnd);
            requestBody.append(lineEnd);

            // Create the HTTP request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/billing/import/csv"))
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .header("Authorization", "Bearer " + token)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(requestBody.toString().getBytes()))
                    .build();

            // Send the request
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // Check if the request was successful
            if (response.statusCode() == 200) {
                // Parse the response
                return objectMapper.readValue(response.body(), ImportResponse.class);
            } else {
                // Parse the error response
                ApiError error = objectMapper.readValue(response.body(), ApiError.class);
                throw new ApiException(error, response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            throw new ApiException("Failed to import from CSV: " + e.getMessage(), e);
        }
    }
}
