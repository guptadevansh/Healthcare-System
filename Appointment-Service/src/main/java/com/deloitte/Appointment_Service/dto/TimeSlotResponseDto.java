package com.deloitte.Appointment_Service.dto;

import java.time.LocalDateTime;

public record TimeSlotResponseDto(
        LocalDateTime slotTime,
        Boolean isAvailable
) {}
