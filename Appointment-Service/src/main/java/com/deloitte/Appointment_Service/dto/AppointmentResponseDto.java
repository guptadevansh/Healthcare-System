package com.deloitte.Appointment_Service.dto;

import com.deloitte.Appointment_Service.constants.AppointmentStatus;

import java.time.LocalDateTime;

public record AppointmentResponseDto(
        Long id,
        Long patientId,
        Long providerId,
        AppointmentStatus status,
        LocalDateTime appointmentDateTime,
        LocalDateTime createdAt,
        String message,
        String errorMessage
) {}
