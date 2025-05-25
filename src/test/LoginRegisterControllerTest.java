package com.example.software.view;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("登录注册控制器测试")
public class LoginRegisterControllerTest {
    
    private LoginRegisterController controller;

    @BeforeEach
    void setUp() {
        controller = new LoginRegisterController();
    }

    @Test
    @DisplayName("密码验证 - 基本规则测试")
    void testPasswordValidation() {
        // 有效密码测试（3-16位）
        assertTrue(controller.validatePassword("123456"), 
            "6位密码应该通过验证");
        
        // 无效密码测试
        assertFalse(controller.validatePassword("12"), 
            "2位密码应该验证失败");
        assertFalse(controller.validatePassword(""), 
            "空密码应该验证失败");
        assertFalse(controller.validatePassword(null), 
            "null密码应该验证失败");
        assertFalse(controller.validatePassword("a".repeat(17)), 
            "超过16位的密码应该验证失败");
    }

    @ParameterizedTest
    @DisplayName("密码验证 - 参数化测试")
    @CsvSource({
        "123456, true, 有效密码",
        "12, false, 太短的密码",
        "'', false, 空密码",
        "abcdefghijklmnopq, false, 太长的密码"
    })
    void testPasswordValidationParameterized(String password, boolean expected, String message) {
        assertEquals(expected, controller.validatePassword(password), message);
    }

    @Test
    @DisplayName("登录凭据验证 - 边界值测试")
    void testCredentialsValidationBoundary() {
        // 测试账号验证
        assertTrue(controller.validateCredentials("test", "test"), 
            "测试账号应该能登录成功");
        
        // 边界值测试
        assertFalse(controller.validateCredentials("", "password"), 
            "空用户名应该验证失败");
        assertFalse(controller.validateCredentials("user", ""), 
            "空密码应该验证失败");
        assertFalse(controller.validateCredentials("a".repeat(17), "password"), 
            "超长用户名应该验证失败");
    }

    @Test
    @DisplayName("注册信息验证 - 等价类测试")
    void testRegistrationValidationEquivalence() {
        // 有效等价类
        assertTrue(controller.validateRegistration(
            "newuser", "password123", "password123", "user@test.com"),
            "有效的注册信息应该通过验证");
        
        // 无效等价类 - 密码不匹配
        assertFalse(controller.validateRegistration(
            "user", "pass123", "pass456", "user@test.com"),
            "密码不匹配应该验证失败");
            
        // 无效等价类 - 无效用户名
        assertFalse(controller.validateRegistration(
            "", "pass123", "pass123", "user@test.com"),
            "空用户名应该验证失败");
    }
} 