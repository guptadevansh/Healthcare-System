package com.deloitte.User_Service.dto;

/**
 * Response payload returned when user credentials are validated.
 */
public record LoginResponseDto(
        Long userId,
        String email,
        String name,
        String role
) {
}


