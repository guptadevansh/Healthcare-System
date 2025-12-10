package com.deloitte.Auth_Service.gateway;

import com.deloitte.Auth_Service.dto.SignupRequestDto;
import com.deloitte.Auth_Service.dto.UserServiceRequestDto;
import com.deloitte.Auth_Service.dto.UserServiceResponseDto;
import com.deloitte.Auth_Service.mapper.UserServiceMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Gateway to communicate with UserService
 */
@Component
public class UserServiceGateway {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceGateway.class);

    private final RestTemplate restTemplate;

    @Value("${user-service.base-url:http://localhost:8081}")
    private String userServiceBaseUrl;

    @Value("${user-service.create-user.endpoint:/api/users/createUser}")
    private String createUserEndpoint;

    public UserServiceGateway(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Calls UserService's createUser API
     */
    public UserServiceResponseDto createUser(SignupRequestDto signupRequest) {
        String url = userServiceBaseUrl + createUserEndpoint;
        
        logger.info("Calling UserService createUser API at: {}", url);
        logger.debug("Request payload for email: {}", signupRequest.getEmail());

        try {
            // Map SignupRequestDto to UserServiceRequestDto
            UserServiceRequestDto userServiceRequest = UserServiceMapper.toUserServiceRequestDto(signupRequest);
            logger.debug("Mapped to UserServiceRequestDto: email={}, name={}", 
                    userServiceRequest.email(), userServiceRequest.name());

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Create request entity
            HttpEntity<UserServiceRequestDto> requestEntity = new HttpEntity<>(userServiceRequest, headers);

            // Make the POST request
            ResponseEntity<UserServiceRequestDto> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    UserServiceRequestDto.class
            );

            logger.info("UserService responded with status: {}", response.getStatusCode());
            
            if (response.getStatusCode() == HttpStatus.OK || 
                response.getStatusCode() == HttpStatus.CREATED) {
                
                // Map UserServiceRequestDto response to UserServiceResponseDto
                UserServiceResponseDto responseDto = mapToUserServiceResponse(response.getBody());
                logger.info("Successfully created user with ID: {}", responseDto.getUserId());
                return responseDto;
            } else {
                logger.error("Unexpected response status from UserService: {}", response.getStatusCode());
                throw new RuntimeException("Failed to create user in UserService. Status: " + response.getStatusCode());
            }

        } catch (HttpClientErrorException e) {
            logger.error("Client error calling UserService: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            String errorMessage = extractErrorMessage(e.getResponseBodyAsString());
            throw new RuntimeException("User creation failed: " + errorMessage, e);
        } catch (HttpServerErrorException e) {
            logger.error("Server error from UserService: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("UserService is currently unavailable. Please try again later.", e);
        } catch (RestClientException e) {
            logger.error("Error calling UserService: ", e);
            throw new RuntimeException("Failed to connect to UserService: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error calling UserService: ", e);
            throw new RuntimeException("An unexpected error occurred while creating user: " + e.getMessage(), e);
        }
    }

    /**
     * Maps UserServiceRequestDto response from User Service to UserServiceResponseDto
     */
    private UserServiceResponseDto mapToUserServiceResponse(UserServiceRequestDto userDto) {
        if (userDto == null) {
            return null;
        }

        UserServiceResponseDto responseDto = new UserServiceResponseDto();
        responseDto.setUserId(userDto.id() != null ? userDto.id().toString() : null);
        responseDto.setName(userDto.name());
        responseDto.setEmail(userDto.email());
        
        // Parse dateOfBirth string to LocalDate
        if (userDto.dateOfBirth() != null && !userDto.dateOfBirth().isEmpty()) {
            try {
                responseDto.setDob(java.time.LocalDate.parse(userDto.dateOfBirth()));
            } catch (Exception e) {
                logger.warn("Failed to parse dateOfBirth: {}", userDto.dateOfBirth());
            }
        }
        
        responseDto.setAddress(userDto.address());
        responseDto.setCreatedAt(java.time.LocalDateTime.now());
        responseDto.setIsActive(true);
        
        return responseDto;
    }

    /**
     * Extracts error message from User Service error response
     */
    private String extractErrorMessage(String responseBody) {
        if (responseBody == null || responseBody.isEmpty()) {
            return "Unknown error from User Service";
        }
        
        try {
            // Try to parse JSON error response
            if (responseBody.contains("\"message\"")) {
                int messageStart = responseBody.indexOf("\"message\"") + 10;
                int messageEnd = responseBody.indexOf("\"", messageStart);
                if (messageEnd > messageStart) {
                    return responseBody.substring(messageStart, messageEnd);
                }
            }
        } catch (Exception e) {
            logger.debug("Could not parse error message from response body");
        }
        return responseBody.length() > 200 ? responseBody.substring(0, 200) + "..." : responseBody;
    }
}

