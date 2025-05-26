package com.example.software.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Real implementation of the summary service
 */
public class RealSummaryService implements SummaryService {

    private static final String BASE_URL = "http://localhost:8080/api/purseai/v1";
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private final String token;

    public RealSummaryService(String token) {
        this.token = token;
    }

    @Override
    public ExpenditureSummary getExpenditureSummary(String period) throws ApiException {
        try {
            // Validate the period
            if (!isValidPeriod(period)) {
                throw new ApiException(
                    new ApiError("INVALID_PERIOD", "Invalid period. Must be Week, Month, or Year"),
                    400
                );
            }

            // Create the HTTP request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/summary/expenditure?period=" + period))
                    .header("Authorization", "Bearer " + token)
                    .GET()
                    .build();

            // Send the request
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // Check if the request was successful
            if (response.statusCode() == 200) {
                // Parse the response
                return objectMapper.readValue(response.body(), ExpenditureSummary.class);
            } else {
                // Parse the error response
                ApiError error = objectMapper.readValue(response.body(), ApiError.class);
                throw new ApiException(error, response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            throw new ApiException("Failed to get expenditure summary: " + e.getMessage(), e);
        }
    }

    @Override
    public IncomeSummary getIncomeSummary(String period) throws ApiException {
        try {
            // Validate the period
            if (!isValidPeriod(period)) {
                throw new ApiException(
                    new ApiError("INVALID_PERIOD", "Invalid period. Must be Week, Month, or Year"),
                    400
                );
            }

            // Create the HTTP request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/summary/income?period=" + period))
                    .header("Authorization", "Bearer " + token)
                    .GET()
                    .build();

            // Send the request
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // Check if the request was successful
            if (response.statusCode() == 200) {
                // Parse the response
                return objectMapper.readValue(response.body(), IncomeSummary.class);
            } else {
                // Parse the error response
                ApiError error = objectMapper.readValue(response.body(), ApiError.class);
                throw new ApiException(error, response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            throw new ApiException("Failed to get income summary: " + e.getMessage(), e);
        }
    }

    /**
     * Check if the period is valid
     */
    private boolean isValidPeriod(String period) {
        return period != null && (period.equals("Week") || period.equals("Month") || period.equals("Year"));
    }
}
