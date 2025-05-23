package com.example.software.api;

import org.springframework.stereotype.Component;

/**
 * Factory for creating API service implementations
 *
 * This factory determines which implementation to return based on the username:
 * - For username "test", it returns mock implementations that work completely offline
 * - For username "admin", it returns real implementations but with special handling during login
 * - For all other usernames, it returns real implementations
 */
@Component
public class ApiServiceFactory {

    private static final String TEST_USERNAME = "test";
    private static final String ADMIN_USERNAME = "admin";

    private static ApiServiceFactory instance;

    private String currentUsername;
    private String token;

    private ApiServiceFactory() {
    }

    /**
     * Get the singleton instance
     *
     * @return The ApiServiceFactory instance
     */
    public static synchronized ApiServiceFactory getInstance() {
        if (instance == null) {
            instance = new ApiServiceFactory();
        }
        return instance;
    }

    /**
     * Set the current user context
     *
     * @param username The username
     * @param token The authentication token
     */
    public void setUserContext(String username, String token) {
        this.currentUsername = username;
        this.token = token;
    }

    /**
     * Clear the current user context
     */
    public void clearUserContext() {
        this.currentUsername = null;
        this.token = null;
    }

    /**
     * Get the current username
     *
     * @return The current username
     */
    public String getCurrentUsername() {
        return currentUsername;
    }

    /**
     * Get the current token
     *
     * @return The current token
     */
    public String getToken() {
        return token;
    }

    /**
     * Check if the current user is the test user
     *
     * @return True if the current user is the test user
     */
    public boolean isTestUser() {
        return TEST_USERNAME.equals(currentUsername);
    }

    /**
     * Check if the current user is the admin user
     *
     * @return True if the current user is the admin user
     */
    public boolean isAdminUser() {
        return ADMIN_USERNAME.equals(currentUsername);
    }

    /**
     * Get the authentication service
     *
     * @return The authentication service
     */
    public AuthService getAuthService() {
        if (isTestUser()) {
            return new MockAuthService();
        } else if (isAdminUser()) {
            return new AdminAuthService();
        } else {
            return new RealAuthService();
        }
    }

    /**
     * Get the savings service
     *
     * @return The savings service
     */
    public SavingsService getSavingsService() {
        if (isTestUser()) {
            return new MockSavingsService();
        } else {
            return new RealSavingsService(token);
        }
    }

    /**
     * Get the billing service
     *
     * @return The billing service
     */
    public BillingService getBillingService() {
        if (isTestUser()) {
            return new MockBillingService();
        } else {
            return new RealBillingService(token);
        }
    }

    /**
     * Get the summary service
     *
     * @return The summary service
     */
    public SummaryService getSummaryService() {
        if (isTestUser()) {
            return new MockSummaryService();
        } else {
            return new RealSummaryService(token);
        }
    }

    /**
     * Get the AI service
     *
     * @return The AI service
     */
    public AIService getAIService() {
        if (isTestUser()) {
            return new MockAIService();
        } else {
            return new RealAIService(token);
        }
    }

    /**
     * Get the user service
     *
     * @return The user service
     */
    public UserService getUserService() {
        if (isTestUser()) {
            return new MockUserService();
        } else {
            return new RealUserService(token);
        }
    }
}
