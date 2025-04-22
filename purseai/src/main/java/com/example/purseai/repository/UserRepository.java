package com.example.purseai.repository;

import com.example.purseai.model.User;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository {
    User save(User user);
    Optional<User> findById(String userId);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    List<User> findAll();
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    void deleteById(String userId);
}