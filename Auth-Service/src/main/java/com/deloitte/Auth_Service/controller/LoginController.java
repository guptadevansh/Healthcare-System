package com.deloitte.Auth_Service.controller;

import com.deloitte.Auth_Service.dto.LoginRequestDto;
import com.deloitte.Auth_Service.dto.LoginResponseDto;
import com.deloitte.Auth_Service.service.LoginService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for user login operations
 */
@RestController
@RequestMapping("/api")
public class LoginController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    private final LoginService loginService;

    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    /**
     * Login endpoint that authenticates user and returns JWT token
     * 
     * @param loginRequest Login credentials (username/email and password)
     * @param bindingResult Validation result
     * @return ResponseEntity with JWT token or error message
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequestDto loginRequest,
            BindingResult bindingResult) {

        logger.info("Received login request for username: {}", loginRequest.getUsername());

        // Check for validation errors
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            for (FieldError error : bindingResult.getFieldErrors()) {
                errors.put(error.getField(), error.getDefaultMessage());
            }
            
            logger.warn("Validation errors in login request: {}", errors);
            return ResponseEntity.badRequest().body(errors);
        }

        // Process login
        LoginResponseDto response = loginService.login(loginRequest);

        if (response.getAccessToken() != null) {
            logger.info("Login successful for user: {}", loginRequest.getUsername());
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } else {
            logger.error("Login failed for user: {} - {}", 
                    loginRequest.getUsername(), response.getErrorMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    /**
     * Health check endpoint for login API
     */
    @GetMapping("/login/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "Auth Service - Login API");
        return ResponseEntity.ok(health);
    }
}

