package com.example.software.repository;

import com.example.software.model.User;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

@Component
public class FileBasedUserRepository implements UserRepository {
    
    private static final Logger logger = Logger.getLogger(FileBasedUserRepository.class.getName());
    private static final String DATA_DIR = "data";
    private static final String USERS_FILE = "users.json";
    
    private final ObjectMapper objectMapper;
    private final Path dataDirectory;
    private final Path usersFilePath;
    
    // 内存缓存
    private final Map<String, User> userCache = new ConcurrentHashMap<>();
    
    public FileBasedUserRepository() {
        this.objectMapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);
            
        this.dataDirectory = Paths.get(DATA_DIR);
        this.usersFilePath = dataDirectory.resolve(USERS_FILE);
        
        initialize();
    }
    
    private void initialize() {
        try {
            // 创建数据目录
            if (!Files.exists(dataDirectory)) {
                Files.createDirectories(dataDirectory);
            }
            
            // 如果用户文件存在，加载到内存
            if (Files.exists(usersFilePath)) {
                List<User> users = objectMapper.readValue(
                    usersFilePath.toFile(),
                    new TypeReference<List<User>>() {}
                );
                
                for (User user : users) {
                    userCache.put(user.getUserId(), user);
                }
                
                logger.info("已加载 " + users.size() + " 个用户记录");
            } else {
                // 创建空文件
                saveToFile();
                logger.info("创建了新的用户存储文件");
            }
        } catch (IOException e) {
            logger.severe("初始化用户数据时出错: " + e.getMessage());
        }
    }
    
    private void saveToFile() {
        try {
            objectMapper.writeValue(usersFilePath.toFile(), userCache.values());
            logger.info("用户数据已保存到文件");
        } catch (IOException e) {
            logger.severe("保存用户数据时出错: " + e.getMessage());
        }
    }
    
    @Override
    public User save(User user) {
        // 如果没有ID则生成一个
        if (user.getUserId() == null || user.getUserId().isEmpty()) {
            user.setUserId(UUID.randomUUID().toString());
        }
        
        userCache.put(user.getUserId(), user);
        saveToFile();
        
        logger.info("保存了用户: " + user.getUsername());
        return user;
    }
    
    @Override
    public Optional<User> findById(String userId) {
        return Optional.ofNullable(userCache.get(userId));
    }
    
    @Override
    public Optional<User> findByUsername(String username) {
        return userCache.values().stream()
            .filter(user -> user.getUsername().equals(username))
            .findFirst();
    }
    
    @Override
    public Optional<User> findByEmail(String email) {
        return userCache.values().stream()
            .filter(user -> user.getEmail().equals(email))
            .findFirst();
    }
    
    @Override
    public List<User> findAll() {
        return new ArrayList<>(userCache.values());
    }
    
    @Override
    public boolean existsByUsername(String username) {
        return userCache.values().stream()
            .anyMatch(user -> user.getUsername().equals(username));
    }
    
    @Override
    public boolean existsByEmail(String email) {
        return userCache.values().stream()
            .anyMatch(user -> user.getEmail().equals(email));
    }
    
    @Override
    public void deleteById(String userId) {
        User removed = userCache.remove(userId);
        if (removed != null) {
            saveToFile();
            logger.info("删除了用户: " + removed.getUsername());
        }
    }
}
