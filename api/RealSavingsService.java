package com.example.software.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Real implementation of the savings service
 */
public class RealSavingsService implements SavingsService {

    private static final String BASE_URL = "http://localhost:8080/api/purseai/v1";
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
            .configure(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_SINGLE_QUOTES, true)
            .findAndRegisterModules();

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
            requestBody.put("startDate", startDate.atStartOfDay().toInstant(ZoneOffset.UTC).toString());
            requestBody.put("cycle", cycle);
            requestBody.put("cycleTimes", cycleTimes);
            requestBody.put("amount", amount);
            requestBody.put("currency", currency);

            // Create the HTTP request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/savings/plans/createPlan"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
                    .build();

            // Send the request
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            System.out.println("创建计划HTTP响应: " + response.statusCode() + ", 正文: " + response.body());

            // Check if the request was successful
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                try {
                    // 尝试直接将响应解析为SavingPlanResponse
                    return objectMapper.readValue(response.body(), SavingPlanResponse.class);
                } catch (Exception e) {
                    System.err.println("解析成功响应时发生错误: " + e.getMessage());
                    e.printStackTrace();
                    
                    // 如果解析为SavingPlanResponse失败，尝试解析为Map然后手动构建响应
                    try {
                        Map<String, Object> responseMap = objectMapper.readValue(response.body(), 
                                new TypeReference<Map<String, Object>>() {});
                                
                        SavingPlanResponse savingPlanResponse = new SavingPlanResponse();
                        savingPlanResponse.setSuccess(true);
                        
                        if (responseMap.containsKey("planId")) {
                            savingPlanResponse.setPlanId((String) responseMap.get("planId"));
                        }
                        if (responseMap.containsKey("name")) {
                            savingPlanResponse.setName((String) responseMap.get("name"));
                        }
                        // 其他字段可以根据需要设置...
                        
                        return savingPlanResponse;
                    } catch (Exception ex) {
                        System.err.println("尝试手动解析响应失败: " + ex.getMessage());
                        ex.printStackTrace();
                        throw new ApiException("无法解析成功响应: " + e.getMessage(), e);
                    }
                }
            } else {
                // 尝试解析错误响应
                try {
                    ApiError error = objectMapper.readValue(response.body(), ApiError.class);
                    throw new ApiException(error, response.statusCode());
                } catch (Exception e) {
                    System.err.println("解析错误响应时发生错误: " + e.getMessage());
                    e.printStackTrace();
                    throw new ApiException("请求失败，HTTP状态码: " + response.statusCode() + ", 响应: " + response.body());
                }
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("发送请求时发生错误: " + e.getMessage());
            e.printStackTrace();
            throw new ApiException("创建储蓄计划失败: " + e.getMessage(), e);
        }
    }

    @Override
    public List<SavingPlan> getAllPlans() throws ApiException {
        try {
            // Create the HTTP request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/savings/plans/getAllPlans"))
                    .header("Authorization", "Bearer " + token)
                    .GET()
                    .build();

            // Send the request
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            System.out.println("获取所有计划HTTP响应: " + response.statusCode());

            // Check if the request was successful
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                try {
                    // Parse the response
                    Map<String, Object> responseMap = objectMapper.readValue(response.body(), new TypeReference<Map<String, Object>>() {});
                    List<SavingPlan> plans = objectMapper.convertValue(responseMap.get("plans"), new TypeReference<List<SavingPlan>>() {});
                    System.out.println("成功获取 " + plans.size() + " 个计划");
                    return plans;
                } catch (Exception e) {
                    System.err.println("解析计划列表响应时出错: " + e.getMessage());
                    e.printStackTrace();
                    throw new ApiException("无法解析计划列表响应: " + e.getMessage());
                }
            } else {
                try {
                    // Parse the error response
                    ApiError error = objectMapper.readValue(response.body(), ApiError.class);
                    throw new ApiException(error, response.statusCode());
                } catch (Exception e) {
                    System.err.println("解析错误响应时出错: " + e.getMessage());
                    e.printStackTrace();
                    throw new ApiException("获取计划列表失败，HTTP状态码: " + response.statusCode() + ", 响应: " + response.body());
                }
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("发送获取计划请求时出错: " + e.getMessage());
            e.printStackTrace();
            throw new ApiException("获取储蓄计划列表失败: " + e.getMessage(), e);
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
            requestBody.put("startDate", startDate.atStartOfDay().toInstant(ZoneOffset.UTC).toString());
            requestBody.put("cycle", cycle);
            requestBody.put("cycleTimes", cycleTimes);
            requestBody.put("amount", amount);
            requestBody.put("currency", currency);
            requestBody.put("savedAmount", savedAmount);
            
            System.out.println("更新计划: ID=" + planId + ", 名称=" + name + 
                              ", 开始日期=" + startDate.atStartOfDay().toInstant(ZoneOffset.UTC).toString() + 
                              ", 周期=" + cycle + 
                              ", 次数=" + cycleTimes + 
                              ", 金额=" + amount + 
                              ", 货币=" + currency + 
                              ", 已存=" + savedAmount);

            // Create the HTTP request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/savings/plans/updatePlan?planId=" + planId))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .PUT(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
                    .build();

            // 记录完整的请求信息，帮助调试
            System.out.println("更新计划请求: " + request.method() + " " + request.uri());
            System.out.println("请求头: " + request.headers());
            System.out.println("请求体: " + objectMapper.writeValueAsString(requestBody));

            // Send the request
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            System.out.println("更新计划HTTP响应: " + response.statusCode() + ", 响应体: " + response.body());

            // Check if the request was successful
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                try {
                    // Parse the response
                    return objectMapper.readValue(response.body(), SavingPlanResponse.class);
                } catch (Exception e) {
                    System.err.println("解析更新计划响应时出错: " + e.getMessage());
                    e.printStackTrace();
                    throw new ApiException("无法解析更新计划响应: " + e.getMessage());
                }
            } else {
                try {
                    // Parse the error response
                    ApiError error = objectMapper.readValue(response.body(), ApiError.class);
                    throw new ApiException(error, response.statusCode());
                } catch (Exception e) {
                    System.err.println("解析错误响应时出错: " + e.getMessage());
                    e.printStackTrace();
                    throw new ApiException("更新计划失败，HTTP状态码: " + response.statusCode() + ", 响应: " + response.body());
                }
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("发送更新计划请求时出错: " + e.getMessage());
            e.printStackTrace();
            throw new ApiException("更新储蓄计划失败: " + e.getMessage(), e);
        }
    }

    @Override
    public ApiResponse deletePlan(String planId) throws ApiException {
        try {
            System.out.println("删除计划: ID=" + planId);
            
            // Create the HTTP request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/savings/plans/deletePlan?planId=" + planId))
                    .header("Authorization", "Bearer " + token)
                    .DELETE()
                    .build();

            // Send the request
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            System.out.println("删除计划HTTP响应: " + response.statusCode());

            // Check if the request was successful
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                try {
                    // Parse the response
                    return objectMapper.readValue(response.body(), ApiResponse.class);
                } catch (Exception e) {
                    System.err.println("解析删除计划响应时出错: " + e.getMessage());
                    e.printStackTrace();
                    
                    // 即使解析失败，删除可能已经成功
                    ApiResponse successResponse = new ApiResponse(true, "计划已删除");
                    return successResponse;
                }
            } else {
                try {
                    // Parse the error response
                    ApiError error = objectMapper.readValue(response.body(), ApiError.class);
                    throw new ApiException(error, response.statusCode());
                } catch (Exception e) {
                    System.err.println("解析错误响应时出错: " + e.getMessage());
                    e.printStackTrace();
                    throw new ApiException("删除计划失败，HTTP状态码: " + response.statusCode() + ", 响应: " + response.body());
                }
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("发送删除计划请求时出错: " + e.getMessage());
            e.printStackTrace();
            throw new ApiException("删除储蓄计划失败: " + e.getMessage(), e);
        }
    }
}
