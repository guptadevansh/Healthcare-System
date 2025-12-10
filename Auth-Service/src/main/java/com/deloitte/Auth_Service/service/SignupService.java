package com.deloitte.Auth_Service.service;

import com.deloitte.Auth_Service.dto.SignupRequestDto;
import com.deloitte.Auth_Service.dto.SignupResponseDto;
import com.deloitte.Auth_Service.dto.UserServiceResponseDto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.deloitte.Auth_Service.gateway.UserServiceGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service layer for user signup operations
 */
@Service
public class SignupService {

    private static final Logger logger = LoggerFactory.getLogger(SignupService.class);

    private final UserServiceGateway userServiceGateway;

    public SignupService(UserServiceGateway userServiceGateway) {
        this.userServiceGateway = userServiceGateway;
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
            // Call UserService to create user
            logger.info("Redirecting signup request to User Service");
            UserServiceResponseDto userServiceResponse = userServiceGateway.createUser(signupRequest);

            logger.info("User created successfully in User Service with userId: {}", userServiceResponse.getUserId());

            // Map UserService response to Signup response
            SignupResponseDto response = mapToSignupResponse(userServiceResponse);
            response.setStatus("SUCCESS");
            response.setMessage("User registered successfully");

            return response;

        } catch (RuntimeException e) {
            logger.error("Error during signup process: ", e);
            
            // Return error response
            SignupResponseDto errorResponse = new SignupResponseDto();
            errorResponse.setEmail(signupRequest.getEmail());
            errorResponse.setStatus("FAILURE");
            errorResponse.setMessage(e.getMessage() != null ? e.getMessage() : "Failed to create user");
            
            return errorResponse;
        }
    }

    /**
     * Maps UserServiceResponseDto to SignupResponseDto
     */
    private SignupResponseDto mapToSignupResponse(UserServiceResponseDto userServiceResponse) {
        SignupResponseDto response = new SignupResponseDto();
        response.setUserId(userServiceResponse.getUserId());
        response.setName(userServiceResponse.getName());
        response.setEmail(userServiceResponse.getEmail());
        response.setDob(userServiceResponse.getDob());
        response.setPhoneNumber(userServiceResponse.getPhoneNumber());
        response.setAddress(userServiceResponse.getAddress());
        response.setCreatedAt(userServiceResponse.getCreatedAt());
        return response;
    }
}

