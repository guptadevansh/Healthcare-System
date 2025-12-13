package com.deloitte.User_Service.dto;

public record UserResponseDto(
        Long id,

        String message,

        String errorMessage
) {
}
