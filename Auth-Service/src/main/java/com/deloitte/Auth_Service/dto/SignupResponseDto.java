package com.deloitte.Auth_Service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for user signup response
 */
@Setter
@Getter
public class SignupResponseDto {

    private String userId;
    private String name;
    private String email;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dob;
    private String phoneNumber;
    private String address;
    private String status;
    private String message;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    public SignupResponseDto() {
    }

    public SignupResponseDto(String userId, String name, String email, String status, String message) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.status = status;
        this.message = message;
        this.createdAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "SignupResponseDto{" +
                "userId='" + userId + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", status='" + status + '\'' +
                ", message='" + message + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}

