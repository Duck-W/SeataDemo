package org.example.authservice.service;

import org.example.authservice.dto.AuthResponse;
import org.example.authservice.dto.LoginRequest;
import org.example.authservice.dto.RegisterRequest;
import org.example.authservice.entity.User;
import org.example.authservice.repository.UserRepository;
import org.example.authservice.security.JwtUtil;
import org.example.authservice.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
public class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("encodedPassword");
        testUser.setRole("USER");
    }

    @Test
    void testRegister_Success() {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setPassword("password");
        registerRequest.setRole("USER");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtUtil.generateToken("testuser", "USER")).thenReturn("testToken");

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("testToken", response.getToken());
        assertEquals("testuser", response.getUser().getUsername());
        assertEquals("USER", response.getUser().getRole());

        verify(userRepository, times(1)).findByUsername("testuser");
        verify(passwordEncoder, times(1)).encode("password");
        verify(userRepository, times(1)).save(any(User.class));
        verify(jwtUtil, times(1)).generateToken("testuser", "USER");
    }

    @Test
    void testRegister_UserAlreadyExists() {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setPassword("password");
        registerRequest.setRole("USER");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        assertThrows(RuntimeException.class, () -> authService.register(registerRequest));

        verify(userRepository, times(1)).findByUsername("testuser");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
        verify(jwtUtil, never()).generateToken(anyString(), anyString());
    }
}