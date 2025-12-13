package com.deloitte.Auth_Service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for user login response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponseDto {

    private String accessToken;
    private String tokenType;
    private Long expiresIn;
    private String message;
    private String errorMessage;
}

