//package com.example.software.service;
//
//import com.example.software.dto.AuthResponse;
//import com.example.software.dto.LoginRequest;
//import com.example.software.dto.RegisterRequest;
//import com.example.software.config.JwtConfig;
//import com.example.software.repository.JsonFileUserRepository;
//import com.example.software.security.UserDetailsImpl;
//import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.SignatureAlgorithm;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.stereotype.Service;
//import com.example.software.model.User;
//import java.io.IOException;
//import java.security.Key;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.UUID;
//@Service
//public class UserService {
//    @Autowired
//    private AuthenticationManager authenticationManager;
//
//    @Autowired
//    private JsonFileUserRepository userRepository;
//    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
//
//    private List<User> users = new ArrayList<>();
//
//    private final JwtConfig jwtSecretKeyService;
//
//    @Autowired
//    public UserService(JwtConfig jwtSecretKeyService) {
//        this.jwtSecretKeyService = jwtSecretKeyService;
//    }
//
//    public User register(RegisterRequest request) throws IOException {
//        if (userRepository.existsByUsername(request.getUsername())) {
//            throw new RuntimeException("Username is already taken!");
//        }
//        User newUser = new User();
//        newUser.setUserId(UUID.randomUUID().toString());
//        newUser.setUsername(request.getUsername());
//        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
//        newUser.setEmail(request.getEmail());
//        newUser.setPhone(request.getPhone());
//
//
//        userRepository.save(newUser);
//
//        Authentication authentication = authenticationManager.authenticate(
//                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
//        );
//
//        return newUser;
//
//
//
//        User user = new User();
//        user.setUserId(UUID.randomUUID().toString());
//        user.setUsername(request.getUsername());
//        user.setPassword(passwordEncoder.encode(request.getPassword()));
//        user.setEmail(request.getEmail());
//
//        userRepository.save(user);
//
//        Authentication authentication = authenticationManager.authenticate(
//                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
//        );
//
//        SecurityContextHolder.getContext().setAuthentication(authentication);
//        String jwt = jwtUtil.generateToken(new UserDetailsImpl(user));
//
//        return new AuthResponse(jwt, "Bearer", user.getUsername());
//    }
//
//    public User login(LoginRequest request) throws IOException {
//        User user = users.stream()
//                .filter(u -> u.getUsername().equals(request.getUsername()))
//                .findFirst()
//                .orElseThrow(() -> new RuntimeException("用户名或密码错误"));
//
//        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
//            throw new RuntimeException("用户名或密码错误");
//        }
//
//        return user;
//    }
//
//    public String generateJwtToken(User user) {
//        // 获取 JwtConfig 中的密钥
//        Key jwtSecretKey = jwtSecretKeyService.getJwtSecretKey();
//
//        // 使用 HS256 算法和密钥生成 JWT Token
//        return Jwts.builder()
//                .setSubject(user.getUsername())
//                .claim("userId", user.getUserId())
//                .signWith(jwtSecretKey, SignatureAlgorithm.HS256)  // 使用正确的密钥
//                .compact();
//    }
//}
