package com.example.purseai.repository;

import com.example.purseai.model.User;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository {
    // 保存用户
    User save(User user);
    
    // 通过ID查找用户
    Optional<User> findById(String userId);
    
    // 通过用户名查找用户
    Optional<User> findByUsername(String username);
    
    // 通过邮箱查找用户
    Optional<User> findByEmail(String email);
    
    // 查找所有用户
    List<User> findAll();
    
    // 检查用户名是否存在
    boolean existsByUsername(String username);
    
    // 检查邮箱是否存在
    boolean existsByEmail(String email);
}