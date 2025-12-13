package com.deloitte.Auth_Service.controller;

import com.deloitte.Auth_Service.dto.SignupRequestDto;
import com.deloitte.Auth_Service.dto.SignupResponseDto;
import com.deloitte.Auth_Service.service.SignupService;
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
 * REST Controller for user signup operations
 */
@RestController
@RequestMapping("/api")
public class SignupController {

    private static final Logger logger = LoggerFactory.getLogger(SignupController.class);

    private final SignupService signupService;

    public SignupController(SignupService signupService) {
        this.signupService = signupService;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(
            @Valid @RequestBody SignupRequestDto signupRequest,
            BindingResult bindingResult) {

        logger.info("Received signup request for email: {}", signupRequest.getEmail());

        // Check for validation errors
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            for (FieldError error : bindingResult.getFieldErrors()) {
                errors.put(error.getField(), error.getDefaultMessage());
            }
            
            logger.warn("Validation errors in signup request: {}", errors);
            return ResponseEntity.badRequest().body(errors);
        }

        // Process signup
        SignupResponseDto response = signupService.signup(signupRequest);

        if (response.getId() != null) {
            logger.info("Signup successful for user: {}", response.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            logger.error("Signup failed: {}", response.getErrorMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Health check endpoint for signup API
     */
    @GetMapping("/signup/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "Auth Service - Signup API");
        return ResponseEntity.ok(health);
    }
}

