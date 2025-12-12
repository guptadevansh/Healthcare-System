package com.deloitte.User_Service.controller;

import com.deloitte.User_Service.dto.AssignRoleRequestDto;
import com.deloitte.User_Service.dto.UserDto;
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
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserDto userRequest) {
        log.info("Received request to create user with email: {}", userRequest.email());
        try {
            UserDto createdUser = userService.createUser(userRequest);
            log.info("User created successfully with ID: {} and email: {}", 
                    createdUser.id(), createdUser.email());
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
}
