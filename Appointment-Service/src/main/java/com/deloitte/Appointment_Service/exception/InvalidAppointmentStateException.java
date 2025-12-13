package com.deloitte.Appointment_Service.exception;

public class InvalidAppointmentStateException extends RuntimeException {
    public InvalidAppointmentStateException(String message) {
        super(message);
    }
}

