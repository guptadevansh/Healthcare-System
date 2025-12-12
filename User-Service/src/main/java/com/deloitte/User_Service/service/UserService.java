package com.deloitte.User_Service.service;

import com.deloitte.User_Service.constants.Role;
import com.deloitte.User_Service.dto.AssignRoleRequestDto;
import com.deloitte.User_Service.dto.UserDto;
import com.deloitte.User_Service.exception.UserAlreadyExistsException;
import com.deloitte.User_Service.exception.ValidationException;
import com.deloitte.User_Service.model.User;
import com.deloitte.User_Service.repository.UserRepository;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Optional;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public UserDto createUser(UserDto request) {
        log.debug("Creating user with email: {}", request.email());
        
        // Validate input
        validateUserRequest(request);
        
        // Check if user already exists
        Optional<User> existingUser = userRepository.findByEmail(request.email());
        if (existingUser.isPresent()) {
            log.warn("Attempt to create user with existing email: {}", request.email());
            throw new UserAlreadyExistsException(
                    String.format("User with email '%s' already exists", request.email())
            );
        }

        try {
            User user = User.builder()
                    .name(request.name())
                    .email(request.email())
                    .contact(request.contact())
                    .dateOfBirth(request.dateOfBirth())
                    .address(request.address())
                    .password(request.password())
                    .gender(request.gender().toUpperCase())
                    .metadata(request.metadata())
                    .build();

            log.debug("Saving user to database: {}", request.email());
            User savedUser = userRepository.save(user);
            log.info("User saved successfully with ID: {}", savedUser.getId());
            
            return mapToDto(savedUser);
        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation while creating user: {}", e.getMessage(), e);
            if (e.getMessage() != null && e.getMessage().contains("email")) {
                throw new UserAlreadyExistsException(
                        String.format("User with email '%s' already exists", request.email())
                );
            }
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while creating user: {}", e.getMessage(), e);
            throw e;
        }
    }

    private void validateUserRequest(UserDto request) {
        if (request.email() == null || request.email().trim().isEmpty()) {
            log.warn("Validation failed: email is null or empty");
            throw new ValidationException("Email is required");
        }
        
        if (request.name() == null || request.name().trim().isEmpty()) {
            log.warn("Validation failed: name is null or empty");
            throw new ValidationException("Name is required");
        }
        
        if (request.password() == null || request.password().trim().isEmpty()) {
            log.warn("Validation failed: password is null or empty");
            throw new ValidationException("Password is required");
        }
        
        // Basic email format validation
        if (!request.email().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+[A-Za-z]{2,}$")) {
            log.warn("Validation failed: invalid email format: {}", request.email());
            throw new ValidationException("Invalid email format");
        }
        
        log.debug("User request validation passed for email: {}", request.email());
    }

    private UserDto mapToDto(User user) {
        return new UserDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getContact(),
                user.getDateOfBirth(),
                user.getAddress(),
                user.getPassword(),
                user.getGender(),
                user.getRole(),
                user.getMetadata()
        );
    }

    public void assignRoleToUser(Long userId, AssignRoleRequestDto userRoleRequest) {
        if(userId == null){
            log.warn("Validation failed: invalid id: {}", (Object) null);
            throw new ValidationException("Invalid id");
        }

        if (!Role.isValid(userRoleRequest.role())) {
            log.warn("Validation failed: invalid role: {}", (Object) null);
            throw new ValidationException("Invalid role. Allowed values: ADMIN, DOCTOR, PATIENT");
        }

        Optional<User> user = userRepository.findById(userId);
        if(user.isPresent()) {
            user.get().setRole(userRoleRequest.role().toUpperCase());
            userRepository.save(user.get());
        }
    }
}
