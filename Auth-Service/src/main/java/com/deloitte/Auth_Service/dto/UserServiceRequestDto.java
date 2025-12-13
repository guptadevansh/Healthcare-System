package com.deloitte.Auth_Service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Map;

/**
 * DTO representing User Service's UserDto structure
 * Used for communication with User Service
 */
public record UserServiceRequestDto(
        Long id,
        @NotBlank(message = "Name is required")
        @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
        String name,
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be a valid email address")
        String email,
        @NotBlank(message = "Contact is required")
        @Size(min = 10, max = 20, message = "Contact must be between 10 and 20 characters")
        String contact,
        @NotBlank(message = "Date of birth is required")
        String dateOfBirth,
        @NotBlank(message = "Address is required")
        @Size(max = 255, message = "Address must not exceed 255 characters")
        String address,
        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters long")
        String password,
        @NotBlank(message = "Gender is required")
        String gender,
        String role,
        Map<String, String> metadata) {
}

