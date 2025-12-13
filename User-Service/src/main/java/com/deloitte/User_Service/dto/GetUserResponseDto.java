package com.deloitte.User_Service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO for getUser API response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetUserResponseDto {
    
    private Long id;
    private String name;
    private String email;
    private String contact;
    private String dateOfBirth;
    private String address;
    private String password;
    private String gender;
    private String role;
    private Map<String, String> metadata;
    private String message;
    private String errorMessage;
}

