package com.example.loginapp.api;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * CSV文件实现的认证服务
 * 将用户名和密码存储在CSV文件中，用于离线认证
 */
public class CsvAuthService implements AuthService {
    private static final String USER_CSV_FILE = "data/user.csv";
    private static final String CSV_DELIMITER = ",";
    private static final String LINE_SEPARATOR = System.lineSeparator();
    
    /**
     * 登录方法，检查用户名和密码是否匹配
     */
    @Override
    public AuthResponse login(String username, String password) throws ApiException {
        if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
            throw new ApiException(
                new ApiError("EMPTY_CREDENTIALS", "用户名和密码不能为空"),
                400
            );
        }
        
        try {
            File file = new File(USER_CSV_FILE);
            if (!file.exists()) {
                System.out.println("CSV文件不存在: " + file.getAbsolutePath());
                throw new ApiException(
                    new ApiError("USER_NOT_FOUND", "用户不存在"),
                    404
                );
            }
            
            System.out.println("正在从CSV文件查找用户: " + username);
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // 跳过空行
                    if (line.trim().isEmpty()) {
                        continue;
                    }
                    String[] parts = line.split(CSV_DELIMITER);
                    System.out.println("读取到用户行: " + line + ", 解析后用户名: " + parts[0]);
                    
                    if (parts.length >= 2 && parts[0].trim().equals(username.trim())) {
                        System.out.println("找到匹配用户名");
                        if (parts[1].trim().equals(password.trim())) {
                            System.out.println("密码匹配成功");
                            // 生成简单的token和userId
                            String userId = "user-" + UUID.randomUUID().toString();
                            String token = "csv-token-" + UUID.randomUUID().toString();
                            return new AuthResponse(userId, username, token, 86400); // 24小时过期
                        } else {
                            System.out.println("密码不匹配: 输入=" + password + ", 存储=" + parts[1]);
                            throw new ApiException(
                                new ApiError("INVALID_PASSWORD", "密码错误"),
                                401
                            );
                        }
                    }
                }
            }
            
            System.out.println("未找到用户: " + username);
            throw new ApiException(
                new ApiError("USER_NOT_FOUND", "用户不存在"),
                404
            );
            
        } catch (IOException e) {
            System.err.println("CSV文件读取错误: " + e.getMessage());
            e.printStackTrace();
            throw new ApiException(
                new ApiError("IO_ERROR", "读取用户文件失败: " + e.getMessage()),
                500
            );
        }
    }
    
    /**
     * 注册方法，将用户名和密码保存到CSV文件
     */
    @Override
    public RegistrationResponse register(String username, String password, String email, String phone) throws ApiException {
        if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
            throw new ApiException(
                new ApiError("EMPTY_CREDENTIALS", "用户名和密码不能为空"),
                400
            );
        }
        
        File file = new File(USER_CSV_FILE);
        
        // 检查用户是否已存在
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // 跳过空行
                    if (line.trim().isEmpty()) {
                        continue;
                    }
                    String[] parts = line.split(CSV_DELIMITER);
                    if (parts.length >= 2 && parts[0].trim().equals(username.trim())) {
                        throw new ApiException(
                            new ApiError("USER_EXISTS", "用户已存在"),
                            409
                        );
                    }
                }
            } catch (IOException e) {
                throw new ApiException(
                    new ApiError("IO_ERROR", "读取用户文件失败: " + e.getMessage()),
                    500
                );
            }
        } else {
            // 确保目录存在
            file.getParentFile().mkdirs();
        }
        
        // 添加新用户
        try (FileWriter writer = new FileWriter(file, true)) {
            String userLine = username + CSV_DELIMITER + password;
            if (email != null && !email.isEmpty()) {
                userLine += CSV_DELIMITER + email;
            }
            if (phone != null && !phone.isEmpty()) {
                userLine += CSV_DELIMITER + phone;
            }
            
            // 使用系统行分隔符而不是简单的\n
            writer.write(userLine + LINE_SEPARATOR);
            System.out.println("已添加新用户到CSV: " + userLine);
            
            // 生成简单的userId和token
            String userId = "user-" + UUID.randomUUID().toString();
            String token = "csv-token-" + UUID.randomUUID().toString();
            
            return new RegistrationResponse(userId, token);
        } catch (IOException e) {
            System.err.println("保存用户信息失败: " + e.getMessage());
            e.printStackTrace();
            throw new ApiException(
                new ApiError("IO_ERROR", "保存用户信息失败: " + e.getMessage()),
                500
            );
        }
    }
    
    /**
     * 验证码验证方法，CSV认证不需要此功能，但需要实现接口方法
     */
    @Override
    public ApiResponse verifyCode(String userId, String code, String type) throws ApiException {
        // CSV认证不使用验证码，直接返回成功
        return new ApiResponse(true, "验证成功");
    }
} 