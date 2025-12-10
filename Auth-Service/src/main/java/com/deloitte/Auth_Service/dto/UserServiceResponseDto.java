package com.deloitte.Auth_Service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for UserService createUser API response
 */
@Setter
@Getter
public class UserServiceResponseDto {

    private String userId;
    private String name;
    private String email;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dob;
    private String phoneNumber;
    private String address;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    private Boolean isActive;

    public UserServiceResponseDto() {
    }

    @Override
    public String toString() {
        return "UserServiceResponseDto{" +
                "userId='" + userId + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}
