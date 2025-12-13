package com.deloitte.Appointment_Service.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateAppointmentStatusRequestDto(
        @NotNull(message = "Action is required (CONFIRM or REJECT)")
        String action
) {}

