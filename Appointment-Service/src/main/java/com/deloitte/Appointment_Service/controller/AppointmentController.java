package com.deloitte.Appointment_Service.controller;

import com.deloitte.Appointment_Service.dto.AppointmentResponseDto;
import com.deloitte.Appointment_Service.dto.CreateAppointmentRequestDto;
import com.deloitte.Appointment_Service.dto.UpdateAppointmentStatusRequestDto;
import com.deloitte.Appointment_Service.service.AppointmentService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private static final Logger log = LoggerFactory.getLogger(AppointmentController.class);

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping("create-appointment")
    public ResponseEntity<AppointmentResponseDto> createAppointment(
            @Valid @RequestBody CreateAppointmentRequestDto request) {
        log.info("Received request to create appointment for patient ID: {}", request.patientId());
        try {
            AppointmentResponseDto response = appointmentService.createAppointment(request);
            log.info("Appointment created successfully with ID: {}", response.id());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error creating appointment for patient ID: {}", request.patientId(), e);
            throw e;
        }
    }

    @PostMapping("/{appointmentId}/provider/{providerId}/update-status")
    public ResponseEntity<AppointmentResponseDto> updateAppointmentStatus(
            @PathVariable Long appointmentId,
            @PathVariable Long providerId,
            @Valid @RequestBody UpdateAppointmentStatusRequestDto request) {
        log.info("Provider ID: {} updating status of appointment ID: {} with action: {}", 
                providerId, appointmentId, request.action());
        try {
            AppointmentResponseDto response;
            
            if ("CONFIRM".equalsIgnoreCase(request.action())) {
                response = appointmentService.confirmAppointment(appointmentId, providerId);
            } else if ("REJECT".equalsIgnoreCase(request.action())) {
                response = appointmentService.rejectAppointment(appointmentId, providerId);
            } else {
                throw new IllegalArgumentException("Invalid action. Use CONFIRM or REJECT");
            }
            
            log.info("Appointment ID: {} status updated successfully", appointmentId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error updating appointment ID: {} by provider ID: {}", 
                    appointmentId, providerId, e);
            throw e;
        }
    }

    @PostMapping("/{appointmentId}/patient/{patientId}/cancel")
    public ResponseEntity<AppointmentResponseDto> cancelAppointment(
            @PathVariable Long appointmentId,
            @PathVariable Long patientId) {
        log.info("Patient ID: {} cancelling appointment ID: {}", patientId, appointmentId);
        try {
            AppointmentResponseDto response = appointmentService.cancelAppointment(
                    appointmentId, patientId);
            log.info("Appointment ID: {} cancelled successfully", appointmentId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error cancelling appointment ID: {} by patient ID: {}", 
                    appointmentId, patientId, e);
            throw e;
        }
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<AppointmentResponseDto>> getPatientAppointments(
            @PathVariable Long patientId) {
        log.info("Received request to fetch appointments for patient ID: {}", patientId);
        try {
            List<AppointmentResponseDto> appointments = 
                    appointmentService.getPatientAppointments(patientId);
            log.info("Retrieved {} appointments for patient ID: {}", 
                    appointments.size(), patientId);
            return ResponseEntity.ok(appointments);
        } catch (Exception e) {
            log.error("Error fetching appointments for patient ID: {}", patientId, e);
            throw e;
        }
    }

    @GetMapping("/provider/{providerId}")
    public ResponseEntity<List<AppointmentResponseDto>> getProviderAppointments(
            @PathVariable Long providerId) {
        log.info("Received request to fetch appointments for provider ID: {}", providerId);
        try {
            List<AppointmentResponseDto> appointments = 
                    appointmentService.getProviderAppointments(providerId);
            log.info("Retrieved {} appointments for provider ID: {}", 
                    appointments.size(), providerId);
            return ResponseEntity.ok(appointments);
        } catch (Exception e) {
            log.error("Error fetching appointments for provider ID: {}", providerId, e);
            throw e;
        }
    }

    @GetMapping("/provider/{providerId}/requested")
    public ResponseEntity<List<AppointmentResponseDto>> getProviderRequestedAppointments(
            @PathVariable Long providerId) {
        log.info("Received request to fetch REQUESTED appointments for provider ID: {}", providerId);
        try {
            List<AppointmentResponseDto> appointments = 
                    appointmentService.getProviderRequestedAppointments(providerId);
            log.info("Retrieved {} REQUESTED appointments for provider ID: {}", 
                    appointments.size(), providerId);
            return ResponseEntity.ok(appointments);
        } catch (Exception e) {
            log.error("Error fetching REQUESTED appointments for provider ID: {}", providerId, e);
            throw e;
        }
    }

    @GetMapping("/{appointmentId}")
    public ResponseEntity<AppointmentResponseDto> getAppointmentById(
            @PathVariable Long appointmentId) {
        log.info("Received request to fetch appointment with ID: {}", appointmentId);
        try {
            AppointmentResponseDto appointment = appointmentService.getAppointmentById(appointmentId);
            log.info("Retrieved appointment with ID: {}", appointmentId);
            return ResponseEntity.ok(appointment);
        } catch (Exception e) {
            log.error("Error fetching appointment with ID: {}", appointmentId, e);
            throw e;
        }
    }
}

