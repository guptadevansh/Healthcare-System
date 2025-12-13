package com.deloitte.Auth_Service.service;

import com.deloitte.Auth_Service.dto.SignupRequestDto;
import com.deloitte.Auth_Service.dto.SignupResponseDto;
import com.deloitte.Auth_Service.dto.UserServiceResponseDto;

import com.deloitte.Auth_Service.gateway.UserServiceGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service layer for user signup operations
 */
@Service
public class SignupService {

    private static final Logger logger = LoggerFactory.getLogger(SignupService.class);

    private final UserServiceGateway userServiceGateway;
    private final PasswordEncoder passwordEncoder;

    public SignupService(UserServiceGateway userServiceGateway) {
        this.userServiceGateway = userServiceGateway;
        // Use BCryptPasswordEncoder specifically for user passwords
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    /**
     * Handles user signup by forwarding the request to UserService
     * 
     * @param signupRequest The signup request containing user details
     * @return SignupResponseDto with the result
     */
    public SignupResponseDto signup(SignupRequestDto signupRequest) {
        logger.info("Processing signup request for email: {}", signupRequest.getEmail());

        try {
            // Hash password before sending to User Service
            if (signupRequest.getPassword() != null) {
                signupRequest.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
            }

            // Call UserService to create user
            logger.info("Redirecting signup request to User Service");
            UserServiceResponseDto userServiceResponse = userServiceGateway.createUser(signupRequest);

            logger.info("User created successfully in User Service with userId: {}", userServiceResponse.getId());

            // Return success response
            SignupResponseDto response = new SignupResponseDto();
            response.setId(userServiceResponse.getId());
            response.setMessage(userServiceResponse.getMessage() != null ? 
                userServiceResponse.getMessage() : "User registered successfully");

            return response;

        } catch (RuntimeException e) {
            logger.error("Error during signup process: ", e);
            
            // Return error response
            SignupResponseDto errorResponse = new SignupResponseDto();
            errorResponse.setErrorMessage(e.getMessage() != null ? e.getMessage() : "Failed to create user");
            errorResponse.setMessage("Signup failed");
            
            return errorResponse;
        }
    }
}

