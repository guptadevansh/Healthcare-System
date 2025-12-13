package com.deloitte.Auth_Service.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO for user signup response
 */
@Setter
@Getter
public class SignupResponseDto {

    private Long id;
    private String message;
    private String errorMessage;
    

    public SignupResponseDto() {
    }

    public SignupResponseDto(Long id, String message, String errorMessage) {
        this.id = id;
        this.message = message;
        this.errorMessage = errorMessage;
    }

    @Override
    public String toString() {
        return "SignupResponseDto{" +
                "id='" + id + '\'' +
                ", message='" + message + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }
}

