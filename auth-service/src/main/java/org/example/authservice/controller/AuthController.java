package org.example.authservice.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.example.authservice.dto.AuthResponse;
import org.example.authservice.dto.LoginRequest;
import org.example.authservice.dto.RegisterRequest;
import org.example.authservice.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Api(tags = "认证管理接口")
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    @ApiOperation(value = "用户登录", notes = "用户登录认证，成功后返回JWT令牌和用户信息")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @ApiParam(name = "loginRequest", value = "登录请求参数", required = true) 
            @Valid @RequestBody LoginRequest loginRequest) {
        logger.info("Received login request for user: {}", loginRequest.getUsername());
        AuthResponse authResponse = authService.login(loginRequest);
        logger.info("Login successful for user: {}", loginRequest.getUsername());
        return ResponseEntity.ok(authResponse);
    }

    @ApiOperation(value = "用户注册", notes = "用户注册，成功后返回JWT令牌和用户信息")
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @ApiParam(name = "registerRequest", value = "注册请求参数", required = true) 
            @Valid @RequestBody RegisterRequest registerRequest) {
        AuthResponse authResponse = authService.register(registerRequest);
        return ResponseEntity.ok(authResponse);
    }
    
    @ApiOperation(value = "测试端点", notes = "无需认证的测试端点")
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Auth service is running!");
    }
}