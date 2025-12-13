package com.deloitte.Auth_Service.dto;

/**
 * Minimal user details returned by User Service after credential validation.
 */
public record UserAuthenticationResponseDto(
        Long userId,
        String email,
        String name,
        String role
) {
}


