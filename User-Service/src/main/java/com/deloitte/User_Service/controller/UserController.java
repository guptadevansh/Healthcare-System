package com.deloitte.User_Service.controller;

import com.deloitte.User_Service.dto.AssignRoleRequestDto;
import com.deloitte.User_Service.dto.GetUserResponseDto;
import com.deloitte.User_Service.dto.UserRequestDto;
import com.deloitte.User_Service.dto.UserResponseDto;
import com.deloitte.User_Service.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/users/createUser")
    public ResponseEntity<UserResponseDto> createUser(@Valid @RequestBody UserRequestDto userRequest) {
        log.info("Received request to create user with email: {}", userRequest.email());
        try {
            UserResponseDto createdUser = userService.createUser(userRequest);
            log.info("User created successfully with ID: {} - {}", 
                    createdUser.id(), createdUser.message());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        } catch (Exception e) {
            log.error("Error creating user with email: {}", userRequest.email(), e);
            throw e;
        }
    }

    @PostMapping("/users/{userId}/assignRole")
    public ResponseEntity<AssignRoleRequestDto> assignRoleToUser(@PathVariable Long userId, @RequestBody AssignRoleRequestDto userRoleRequest) {
        log.info("Received request to assign role user with id: {}", userId);
        try {
            userService.assignRoleToUser(userId, userRoleRequest);
            log.info("Role assigned successfully to user with id: {}",userId);
            return ResponseEntity.status(HttpStatus.OK).body(userRoleRequest);
        } catch (Exception e) {
            log.error("Error assigning role to user with id: {}", userId, e);
            throw e;
        }
    }

    @GetMapping("/users/getUser")
    public ResponseEntity<GetUserResponseDto> getUserByEmail(@RequestParam String email) {
        log.info("Received request to get user with email: {}", email);
        try {
            GetUserResponseDto user = userService.getUserByEmail(email);
            log.info("User retrieved successfully with email: {}", email);
            return ResponseEntity.status(HttpStatus.OK).body(user);
        } catch (Exception e) {
            log.error("Error retrieving user with email: {}", email, e);
            throw e;
        }
    }
}
