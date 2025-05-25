package com.example.loginapp.controller;

/**
 * 所有需要接收 JWT token 的控制器都可以实现这个接口，
 * 以便在 BaseViewController 中统一注入 token。
 */
public interface TokenAwareController {
    void setAuthToken(String token);
}
