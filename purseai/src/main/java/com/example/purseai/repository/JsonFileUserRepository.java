package com.example.purseai.repository;

import com.example.purseai.model.User;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Repository;
import org.springframework.util.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class JsonFileUserRepository {
    private static final Logger logger = LoggerFactory.getLogger(JsonFileUserRepository.class);
    private static final String JSON_FILE_PATH = "data/users.json";
    private final ObjectMapper objectMapper;
    private List<User> users;

    public JsonFileUserRepository() {
        this.objectMapper = new ObjectMapper();
        this.users = new ArrayList<>();
    }

    @PostConstruct
    public void init() {
        loadUsers();
    }

    private void loadUsers() {
        try {
            // 首先尝试从类路径加载
            File file = new File(JSON_FILE_PATH);
            if (!file.exists()) {
                logger.info("File not found at {}. Creating new file.", file.getAbsolutePath());
                file.getParentFile().mkdirs();
                objectMapper.writeValue(file, new ArrayList<User>());
            }
            logger.info("Loading users from {}", file.getAbsolutePath());
            users = objectMapper.readValue(file, new TypeReference<List<User>>() {});
            logger.info("Loaded {} users", users.size());
        } catch (IOException e) {
            logger.error("Error loading users from JSON file: {}", e.getMessage());
            throw new RuntimeException("Error loading users from JSON file", e);
        }
    }

    private void saveUsers() {
        try {
            File file = new File(JSON_FILE_PATH);
            objectMapper.writeValue(file, users);
        } catch (IOException e) {
            logger.error("Error saving users to JSON file: {}", e.getMessage());
            throw new RuntimeException("Error saving users to JSON file", e);
        }
    }

    public Optional<User> findByUsername(String username) {
        return users.stream()
                .filter(user -> user.getUsername().equals(username))
                .findFirst();
    }

    public User save(User user) {
        users.add(user);
        saveUsers();
        return user;
    }

    public boolean existsByUsername(String username) {
        return users.stream().anyMatch(user -> user.getUsername().equals(username));
    }

    public boolean existsByEmail(String email) {
        return users.stream().anyMatch(user -> user.getEmail().equals(email));
    }
} 