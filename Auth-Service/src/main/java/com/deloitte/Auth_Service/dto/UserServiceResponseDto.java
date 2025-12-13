package com.deloitte.Auth_Service.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO for UserService createUser API response
 */
@Setter
@Getter
public class UserServiceResponseDto {

    private Long id;
    private String message;
    private String errorMessage;

    public UserServiceResponseDto(Long id, String message, String errorMessage) {
        this.id = id;
        this.message = message;
        this.errorMessage = errorMessage;
    }
    
    @Override
    public String toString() {
        return "UserServiceResponseDto{" +
                "id=" + id +
                ", message='" + message + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }
}
