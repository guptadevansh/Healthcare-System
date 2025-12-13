package com.deloitte.Auth_Service.service;

import com.deloitte.Auth_Service.dto.GetUserResponseDto;
import com.deloitte.Auth_Service.dto.LoginRequestDto;
import com.deloitte.Auth_Service.dto.LoginResponseDto;
import com.deloitte.Auth_Service.gateway.UserServiceGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Map;

/**
 * Service layer for user login operations
 */
@Service
public class LoginService {

    private static final Logger logger = LoggerFactory.getLogger(LoginService.class);

    private final UserServiceGateway userServiceGateway;
    private final PasswordEncoder passwordEncoder;
    private final RestTemplate restTemplate;

    @Value("${auth-service.oauth2-token.url:http://localhost:9000/oauth2/token}")
    private String oauth2TokenUrl;

    public LoginService(UserServiceGateway userServiceGateway, RestTemplate restTemplate) {
        this.userServiceGateway = userServiceGateway;
        this.restTemplate = restTemplate;
        // Use BCryptPasswordEncoder for password validation
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    /**
     * Handles user login by validating credentials and generating JWT token
     * 
     * @param loginRequest The login request containing username (email) and password
     * @return LoginResponseDto with JWT token or error message
     */
    public LoginResponseDto login(LoginRequestDto loginRequest) {
        logger.info("Processing login request for username: {}", loginRequest.getUsername());

        try {
            // Step 1: Call getUser API in User Service to retrieve user details
            logger.info("Fetching user details from User Service for email: {}", loginRequest.getUsername());
            GetUserResponseDto userResponse = userServiceGateway.getUserByEmail(loginRequest.getUsername());

            if (userResponse == null) {
                logger.error("User not found with email: {}", loginRequest.getUsername());
                return LoginResponseDto.builder()
                        .errorMessage("Invalid username or password")
                        .message("Login failed")
                        .build();
            }

            logger.info("User retrieved: email={}, role={}", userResponse.getEmail(), userResponse.getRole());

            // Step 2: Validate password by comparing with encrypted password from database
            String encryptedPassword = userResponse.getPassword();
            String providedPassword = loginRequest.getPassword();

            if (encryptedPassword == null || !passwordEncoder.matches(providedPassword, encryptedPassword)) {
                logger.warn("Password validation failed for user: {}", loginRequest.getUsername());
                return LoginResponseDto.builder()
                        .errorMessage("Invalid username or password")
                        .message("Login failed")
                        .build();
            }

            logger.info("Password validated successfully for user: {}", loginRequest.getUsername());

            // Step 3: Check if role is not null
            if (userResponse.getRole() == null || userResponse.getRole().trim().isEmpty()) {
                logger.warn("User role is null or empty for user: {}", loginRequest.getUsername());
                return LoginResponseDto.builder()
                        .errorMessage("User role not assigned. Please contact administrator.")
                        .message("Login failed")
                        .build();
            }

            logger.info("User role validated: {}", userResponse.getRole());

            // Step 4: Call OAuth2 token API to generate JWT token
            // Use email as clientId and set role value in the token
            logger.info("Generating JWT token for user: {} with role: {}", 
                    loginRequest.getUsername(), userResponse.getRole());
            
            String jwtToken = generateJwtToken(userResponse.getEmail(), userResponse.getRole());

            if (jwtToken == null) {
                logger.error("Failed to generate JWT token for user: {}", loginRequest.getUsername());
                return LoginResponseDto.builder()
                        .errorMessage("Failed to generate authentication token")
                        .message("Login failed")
                        .build();
            }

            logger.info("JWT token generated successfully for user: {}", loginRequest.getUsername());

            // Step 5: Return the JWT token as login response
            return LoginResponseDto.builder()
                    .accessToken(jwtToken)
                    .tokenType("Bearer")
                    .expiresIn(3600L) // 1 hour in seconds
                    .message("Login successful")
                    .build();

        } catch (RuntimeException e) {
            logger.error("Error during login process for user: {}", loginRequest.getUsername(), e);
            
            // Return error response
            return LoginResponseDto.builder()
                    .errorMessage(e.getMessage() != null ? e.getMessage() : "Login failed")
                    .message("Login failed")
                    .build();
        }
    }

    /**
     * Generates JWT token by calling OAuth2 token endpoint
     * 
     * @param email User's email (used as clientId)
     * @param role User's role (added as claim in JWT)
     * @return JWT access token
     */
    private String generateJwtToken(String email, String role) {
        logger.debug("Calling OAuth2 token endpoint: {}", oauth2TokenUrl);

        try {
            // Prepare request headers with Basic Auth
            // clientId = email, clientSecret = "any" (as per DynamicClientRepository)
            String credentials = email + ":any";
            String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("Authorization", "Basic " + encodedCredentials);

            // Prepare request body with grant_type and role parameter
            MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
            requestBody.add("grant_type", "client_credentials");
            requestBody.add("role", role); // Add role as additional parameter

            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

            // Make POST request to OAuth2 token endpoint
            @SuppressWarnings("rawtypes")
            ResponseEntity<Map> response = restTemplate.exchange(
                    oauth2TokenUrl,
                    HttpMethod.POST,
                    requestEntity,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> responseBody = response.getBody();
                if (responseBody != null) {
                    String accessToken = (String) responseBody.get("access_token");
                    logger.info("JWT token generated successfully");
                    return accessToken;
                } else {
                    logger.error("Response body is null");
                    return null;
                }
            } else {
                logger.error("Failed to generate JWT token. Status: {}", response.getStatusCode());
                return null;
            }

        } catch (HttpClientErrorException e) {
            logger.error("Client error calling OAuth2 token endpoint: {} - {}", 
                    e.getStatusCode(), e.getResponseBodyAsString());
            return null;
        } catch (Exception e) {
            logger.error("Error generating JWT token: ", e);
            return null;
        }
    }
}

