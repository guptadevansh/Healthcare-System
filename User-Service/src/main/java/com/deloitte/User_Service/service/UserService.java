package com.deloitte.User_Service.service;

import com.deloitte.User_Service.constants.Role;
import com.deloitte.User_Service.dto.AssignRoleRequestDto;
import com.deloitte.User_Service.dto.GetUserResponseDto;
import com.deloitte.User_Service.dto.UserRequestDto;
import com.deloitte.User_Service.dto.UserResponseDto;
import com.deloitte.User_Service.exception.UserAlreadyExistsException;
import com.deloitte.User_Service.exception.ValidationException;
import com.deloitte.User_Service.model.Patient;
import com.deloitte.User_Service.model.Provider;
import com.deloitte.User_Service.model.User;
import com.deloitte.User_Service.repository.PatientRepository;
import com.deloitte.User_Service.repository.ProviderRepository;
import com.deloitte.User_Service.repository.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final ProviderRepository providerRepository;

    public UserService(UserRepository userRepository, PatientRepository patientRepository, ProviderRepository providerRepository) {
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
        this.providerRepository = providerRepository;
    }

    @Transactional
    public UserResponseDto createUser(UserRequestDto request) {
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
            
            return new UserResponseDto(savedUser.getId(), "User created successfully", null);
        } catch (Exception e) {
            log.error("Unexpected error while creating user: {}", e.getMessage(), e);
            throw e;
        }
    }

    private void validateUserRequest(UserRequestDto request) {
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

    public void assignRoleToUser(Long userId, AssignRoleRequestDto userRoleRequest) {
        if(userId == null){
            log.warn("Validation failed: invalid id: {}", (Object) null);
            throw new ValidationException("Invalid id");
        }

        if (!Role.isValid(userRoleRequest.role())) {
            log.warn("Validation failed: invalid role: {}", (Object) null);
            throw new ValidationException("Invalid role. Allowed values: ADMIN, PROVIDER, PATIENT, OPS");
        }

        Optional<User> user = userRepository.findById(userId);
        if(user.isPresent()) {
            String role = userRoleRequest.role().toUpperCase();
            user.get().setRole(role);
            userRepository.save(user.get());

            // Populate patient or provider data based on assigned role
            if ("PATIENT".equals(role)) {
                populatePatientData(user.get());
            } else if ("PROVIDER".equals(role)) {
                populateProviderData(user.get());
            }

            log.info("Role assigned successfully to user {} with role: {}", userId, role);
        }
        else{
            log.warn("User not found with id: {}", userId);
            throw new ValidationException("User not found");
        }
    }

    /**
     * Get user by email for login authentication
     */
    public GetUserResponseDto getUserByEmail(String email) {
        log.debug("Fetching user with email: {}", email);
        
        if (email == null || email.trim().isEmpty()) {
            log.warn("Validation failed: email is null or empty");
            throw new ValidationException("Email is required");
        }
        
        Optional<User> userOptional = userRepository.findByEmail(email);
        
        if (userOptional.isEmpty()) {
            log.warn("User not found with email: {}", email);
            throw new ValidationException("User not found with email: " + email);
        }
        
        User user = userOptional.get();
        log.info("User found with email: {}, role: {}", email, user.getRole());
        
        return GetUserResponseDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .contact(user.getContact())
                .dateOfBirth(user.getDateOfBirth())
                .address(user.getAddress())
                .password(user.getPassword())
                .gender(user.getGender())
                .role(user.getRole())
                .metadata(user.getMetadata())
                .message("User retrieved successfully")
                .build();
    }

    /**
     * Populate patient data in patients table when role is assigned as PATIENT
     */
    @Transactional
    public void populatePatientData(User user) {
        try {
            // Check if patient record already exists
            Optional<Patient> existingPatient = patientRepository.findByUser_id(user.getId());

            if (existingPatient.isPresent()) {
                log.debug("Patient record already exists for user id: {}", user.getId());
                return;
            }

            // Create new patient record
            Patient patient = Patient.builder()
                    .user_id(user)
                    .mrn(generateMRN(user.getId()))
                    .build();

            patientRepository.save(patient);
            log.info("Patient data populated successfully for user id: {} with MRN: {}", user.getId(), patient.getMrn());
        } catch (Exception e) {
            log.error("Error while populating patient data for user id: {}, error: {}", user.getId(), e.getMessage(), e);
            throw new RuntimeException("Failed to populate patient data: " + e.getMessage(), e);
        }
    }

    /**
     * Populate provider data in providers table when role is assigned as PROVIDER
     */
    @Transactional
    public void populateProviderData(User user) {
        try {
            // Check if provider record already exists
            Optional<Provider> existingProvider = providerRepository.findByUser_id(user.getId());

            if (existingProvider.isPresent()) {
                log.debug("Provider record already exists for user id: {}", user.getId());
                return;
            }

            // Extract provider details from user metadata or use defaults
            String speciality = (user.getMetadata() != null && user.getMetadata().containsKey("speciality"))
                    ? user.getMetadata().get("speciality")
                    : "General";
            String licenseNo = (user.getMetadata() != null && user.getMetadata().containsKey("license_no"))
                    ? user.getMetadata().get("license_no")
                    : "PENDING";
            String department = (user.getMetadata() != null && user.getMetadata().containsKey("department"))
                    ? user.getMetadata().get("department")
                    : "General";

            // Create new provider record
            Provider provider = Provider.builder()
                    .user_id(user)
                    .speciality(speciality)
                    .license_no(licenseNo)
                    .department(department)
                    .build();

            providerRepository.save(provider);
            log.info("Provider data populated successfully for user id: {} with speciality: {}, department: {}",
                    user.getId(), speciality, department);
        } catch (Exception e) {
            log.error("Error while populating provider data for user id: {}, error: {}", user.getId(), e.getMessage(), e);
            throw new RuntimeException("Failed to populate provider data: " + e.getMessage(), e);
        }
    }

    /**
     * Generate Medical Record Number (MRN) for patient
     */
    private String generateMRN(Long userId) {
        return String.format("MRN-%d-%s", userId, System.currentTimeMillis());
    }
}
