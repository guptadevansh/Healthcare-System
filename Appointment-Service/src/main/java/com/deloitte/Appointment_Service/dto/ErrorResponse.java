package com.deloitte.Appointment_Service.dto;

import java.time.LocalDateTime;

public record ErrorResponse(
    String message,
    String error,
    int status,
    LocalDateTime timestamp
) {}
