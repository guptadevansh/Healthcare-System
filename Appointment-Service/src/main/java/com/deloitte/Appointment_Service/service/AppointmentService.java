package com.deloitte.Appointment_Service.service;

import com.deloitte.Appointment_Service.constants.AppointmentStatus;
import com.deloitte.Appointment_Service.dto.AppointmentResponseDto;
import com.deloitte.Appointment_Service.dto.CreateAppointmentRequestDto;
import com.deloitte.Appointment_Service.exception.AppointmentNotFoundException;
import com.deloitte.Appointment_Service.exception.InvalidAppointmentStateException;
import com.deloitte.Appointment_Service.exception.SlotNotAvailableException;
import com.deloitte.Appointment_Service.model.Appointment;
import com.deloitte.Appointment_Service.repository.AppointmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AppointmentService {

    private static final Logger log = LoggerFactory.getLogger(AppointmentService.class);

    private final AppointmentRepository appointmentRepository;
    private final ProviderScheduleService scheduleService;

    public AppointmentService(AppointmentRepository appointmentRepository,
                            ProviderScheduleService scheduleService) {
        this.appointmentRepository = appointmentRepository;
        this.scheduleService = scheduleService;
    }

    @Transactional
    public AppointmentResponseDto createAppointment(CreateAppointmentRequestDto request) {
        log.info("Creating appointment for patient ID: {} with provider ID: {} for time slot: {}", 
                request.patientId(), request.providerId(), request.appointmentDateTime());

        // Validate that the appointment is in the future
        if (request.appointmentDateTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Cannot book appointments in the past");
        }

        // Check if the slot is available
        if (!scheduleService.isSlotAvailable(request.providerId(), request.appointmentDateTime())) {
            throw new SlotNotAvailableException(
                    "Time slot is not available for booking for time slot: " + request.appointmentDateTime());
        }

        // Create the appointment
        Appointment appointment = Appointment.builder()
                .patientId(request.patientId())
                .providerId(request.providerId())
                .status(AppointmentStatus.REQUESTED)
                .appointmentDateTime(request.appointmentDateTime())
                .build();

        appointment = appointmentRepository.save(appointment);

        // Mark the slot as unavailable
        scheduleService.updateSlotAvailability(
                request.providerId(), 
                request.appointmentDateTime(), 
                false);

        log.info("Appointment created with ID: {} in REQUESTED state", appointment.getId());

        return mapToAppointmentResponseDto(appointment, "Appointment requested successfully", null);
    }

    @Transactional
    public AppointmentResponseDto confirmAppointment(Long appointmentId, Long providerId) {
        log.info("Provider ID: {} confirming appointment ID: {}", providerId, appointmentId);

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException(
                        "Appointment not found with ID: " + appointmentId));

        // Verify provider owns this appointment
        if (!appointment.getProviderId().equals(providerId)) {
            throw new InvalidAppointmentStateException(
                    "Appointment does not belong to this provider");
        }

        // Verify appointment is in REQUESTED state
        if (appointment.getStatus() != AppointmentStatus.REQUESTED) {
            throw new InvalidAppointmentStateException(
                    "Only REQUESTED appointments can be confirmed. Current state: " + 
                    appointment.getStatus());
        }

        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointment = appointmentRepository.save(appointment);

        log.info("Appointment ID: {} confirmed successfully", appointmentId);

        return mapToAppointmentResponseDto(appointment, "Appointment confirmed successfully", null);
    }

    @Transactional
    public AppointmentResponseDto rejectAppointment(Long appointmentId, Long providerId) {
        log.info("Provider ID: {} rejecting appointment ID: {}", providerId, appointmentId);

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException(
                        "Appointment not found with ID: " + appointmentId));

        // Verify provider owns this appointment
        if (!appointment.getProviderId().equals(providerId)) {
            throw new InvalidAppointmentStateException(
                    "Appointment does not belong to this provider");
        }

        // Verify appointment is in REQUESTED state
        if (appointment.getStatus() != AppointmentStatus.REQUESTED) {
            throw new InvalidAppointmentStateException(
                    "Only REQUESTED appointments can be rejected. Current state: " + 
                    appointment.getStatus());
        }

        appointment.setStatus(AppointmentStatus.REJECTED);
        appointment = appointmentRepository.save(appointment);

        // Release the time slot
        scheduleService.updateSlotAvailability(
                appointment.getProviderId(),
                appointment.getAppointmentDateTime(),
                true);

        log.info("Appointment ID: {} rejected and slot released", appointmentId);

        return mapToAppointmentResponseDto(appointment, "Appointment rejected successfully", null);
    }

    @Transactional
    public AppointmentResponseDto cancelAppointment(Long appointmentId, Long patientId) {
        log.info("Patient ID: {} cancelling appointment ID: {}", patientId, appointmentId);

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException(
                        "Appointment not found with ID: " + appointmentId));

        // Verify patient owns this appointment
        if (!appointment.getPatientId().equals(patientId)) {
            throw new InvalidAppointmentStateException(
                    "Appointment does not belong to this patient");
        }

        // Verify appointment can be cancelled (REQUESTED or CONFIRMED state)
        if (appointment.getStatus() != AppointmentStatus.REQUESTED && 
            appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new InvalidAppointmentStateException(
                    "Only REQUESTED or CONFIRMED appointments can be cancelled. Current state: " + 
                    appointment.getStatus());
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment = appointmentRepository.save(appointment);

        // Release the time slot
        scheduleService.updateSlotAvailability(
                appointment.getProviderId(),
                appointment.getAppointmentDateTime(),
                true);

        log.info("Appointment ID: {} cancelled by patient and slot released", appointmentId);

        return mapToAppointmentResponseDto(appointment, "Appointment cancelled successfully", null);
    }

    public List<AppointmentResponseDto> getPatientAppointments(Long patientId) {
        log.info("Fetching appointments for patient ID: {}", patientId);
        
        List<Appointment> appointments = appointmentRepository.findByPatientId(patientId);
        
        return appointments.stream()
                .map(appointment -> mapToAppointmentResponseDto(appointment, null, null))
                .collect(Collectors.toList());
    }

    public List<AppointmentResponseDto> getProviderAppointments(Long providerId) {
        log.info("Fetching appointments for provider ID: {}", providerId);
        
        List<Appointment> appointments = appointmentRepository.findByProviderId(providerId);
        
        return appointments.stream()
                .map(appointment -> mapToAppointmentResponseDto(appointment, null, null))
                .collect(Collectors.toList());
    }

    public List<AppointmentResponseDto> getProviderRequestedAppointments(Long providerId) {
        log.info("Fetching REQUESTED appointments for provider ID: {}", providerId);
        
        List<Appointment> appointments = appointmentRepository
                .findByProviderIdAndStatus(providerId, AppointmentStatus.REQUESTED);
        
        return appointments.stream()
                .map(appointment -> mapToAppointmentResponseDto(appointment, null, null))
                .collect(Collectors.toList());
    }

    public AppointmentResponseDto getAppointmentById(Long appointmentId) {
        log.info("Fetching appointment with ID: {}", appointmentId);
        
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException(
                        "Appointment not found with ID: " + appointmentId));
        
        return mapToAppointmentResponseDto(appointment, null, null);
    }

    private AppointmentResponseDto mapToAppointmentResponseDto(Appointment appointment, String message, String errorMessage) {
        return new AppointmentResponseDto(
                appointment.getId(),
                appointment.getPatientId(),
                appointment.getProviderId(),
                appointment.getStatus(),
                appointment.getAppointmentDateTime(),
                appointment.getCreatedAt(),
                message,
                errorMessage
        );
    }
}
