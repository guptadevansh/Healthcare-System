package com.deloitte.Appointment_Service.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

public record CreateScheduleRequestDto(
        @NotNull(message = "Provider ID is required")
        Long providerId,
        
        @NotNull(message = "Schedule date is required")
        LocalDate scheduleDate,

        @NotNull(message = "Slots are required")
        @JdbcTypeCode(SqlTypes.JSON)
        Map<String, Boolean> slots
) {}

