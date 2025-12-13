package com.deloitte.Appointment_Service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CreateAppointmentRequestDto(
        @NotNull(message = "Patient ID is required")
        Long patientId,
        
        @NotNull(message = "Provider ID is required")
        Long providerId,
        
        @NotNull(message = "Appointment date time is required")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
        LocalDateTime appointmentDateTime
) {}
