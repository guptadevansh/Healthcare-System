package com.deloitte.Auth_Service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for user login request
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDto {

    @NotBlank(message = "Username (email) is required")
    @Email(message = "Username should be a valid email")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;
}

