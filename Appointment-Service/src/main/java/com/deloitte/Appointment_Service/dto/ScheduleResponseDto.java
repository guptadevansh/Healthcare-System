package com.deloitte.Appointment_Service.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record ScheduleResponseDto(
        Long id,
        Long providerId,
        LocalDate scheduleDate,
        Map<String, Boolean> slots,
        String message,
        String errorMessage
) {}
