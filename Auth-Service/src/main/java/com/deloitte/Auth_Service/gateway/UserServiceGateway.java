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

import java.util.Objects;

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
            ResponseEntity<UserServiceResponseDto> responseDto = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    UserServiceResponseDto.class
            );

            logger.info("UserService responded with status: {}", responseDto.getStatusCode());
            
            if (responseDto.getStatusCode() == HttpStatus.OK ||
                    responseDto.getStatusCode() == HttpStatus.CREATED) {
                
                // Map UserServiceRequestDto response to UserServiceResponseDto
//                UserServiceResponseDto responseDto = mapToUserServiceResponse(response.getBody());
                logger.info("Successfully created user with ID: {}", Objects.requireNonNull(responseDto.getBody()).getUserId());
                return responseDto.getBody();
            } else {
                logger.error("Unexpected response status from UserService: {}", responseDto.getStatusCode());
                throw new RuntimeException("Failed to create user in UserService. Status: " + responseDto.getStatusCode());
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
    private UserServiceResponseDto mapToUserServiceResponse(UserServiceResponseDto userDto) {
        if (userDto == null) {
            return null;
        }

        UserServiceResponseDto responseDto = new UserServiceResponseDto();
        responseDto.setUserId(userDto.getUserId() != null ? userDto.getUserId() : null);
        responseDto.setName(userDto.getName());
        responseDto.setEmail(userDto.getEmail());
        
        // Parse dateOfBirth string to LocalDate
        if (userDto.getDob() != null) {
            try {
                responseDto.setDob(userDto.getDob());
            } catch (Exception e) {
                logger.warn("Failed to parse dateOfBirth: {}", userDto.getDob());
            }
        }
        
        responseDto.setAddress(userDto.getAddress());
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

