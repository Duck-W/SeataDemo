package org.example.authservice.service.impl;

import org.example.authservice.dto.AuthResponse;
import org.example.authservice.dto.LoginRequest;
import org.example.authservice.dto.RegisterRequest;
import org.example.authservice.entity.User;
import org.example.authservice.repository.UserRepository;
import org.example.authservice.security.JwtUtil;
import org.example.authservice.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class AuthServiceImpl implements AuthService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);
    
    // JWT生成超时时间（毫秒）
    private static final int JWT_GENERATION_TIMEOUT = 5000;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    @Qualifier("jwtExecutor")
    private Executor jwtExecutor;

    @Override
    public AuthResponse login(LoginRequest loginRequest) {
        try {
            logger.info("Attempting to authenticate user: {}", loginRequest.getUsername());
            
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            User user = userRepository.findByUsername(loginRequest.getUsername())
                    .orElseThrow(() -> {
                        logger.warn("User not found in database: {}", loginRequest.getUsername());
                        return new BadCredentialsException("Invalid username or password");
                    });
            
            logger.info("User found: {} with role: {}", user.getUsername(), user.getRole());
            
            // 使用专用线程池异步生成JWT令牌并设置超时
            CompletableFuture<String> tokenFuture = CompletableFuture.supplyAsync(() -> 
                jwtUtil.generateToken(user.getUsername(), user.getRole()), jwtExecutor
            );
            
            String token;
            try {
                token = tokenFuture.get(JWT_GENERATION_TIMEOUT, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                logger.error("JWT generation timeout for user: {}", user.getUsername());
                throw new BadCredentialsException("Authentication service timeout");
            } catch (ExecutionException e) {
                logger.error("JWT generation failed for user: {}", user.getUsername(), e.getCause());
                throw new BadCredentialsException("Failed to generate authentication token");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("JWT generation interrupted for user: {}", user.getUsername());
                throw new BadCredentialsException("Authentication service interrupted");
            }
            
            AuthResponse.UserInfo userInfo = new AuthResponse.UserInfo(
                    user.getId(),
                    user.getUsername(),
                    user.getRole()
            );
            
            logger.info("Successfully generated token for user: {}", user.getUsername());
            
            return new AuthResponse(token, userInfo);
        } catch (Exception e) {
            logger.error("Authentication failed for user: {}", loginRequest.getUsername(), e);
            throw new BadCredentialsException("Invalid username or password: " + e.getMessage());
        }
    }

    @Override
    public AuthResponse register(RegisterRequest registerRequest) {
        // Check if username already exists
        if (userRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
            throw new RuntimeException("Username is already taken");
        }

        // Create new user
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setRole(registerRequest.getRole());

        User savedUser = userRepository.save(user);

        // Generate token with timeout protection
        String token;
        try {
            CompletableFuture<String> tokenFuture = CompletableFuture.supplyAsync(() -> 
                jwtUtil.generateToken(savedUser.getUsername(), savedUser.getRole()), jwtExecutor
            );
            
            token = tokenFuture.get(JWT_GENERATION_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            logger.error("Failed to generate token for registered user: {}", savedUser.getUsername(), e);
            // 即使令牌生成失败，也要返回用户信息
            token = "";
        }

        AuthResponse.UserInfo userInfo = new AuthResponse.UserInfo(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getRole()
        );

        return new AuthResponse(token, userInfo);
    }
}