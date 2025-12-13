package com.deloitte.Appointment_Service.controller;

import com.deloitte.Appointment_Service.dto.CreateScheduleRequestDto;
import com.deloitte.Appointment_Service.dto.ScheduleResponseDto;
import com.deloitte.Appointment_Service.dto.TimeSlotResponseDto;
import com.deloitte.Appointment_Service.service.ProviderScheduleService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/schedules")
public class ProviderScheduleController {

    private static final Logger log = LoggerFactory.getLogger(ProviderScheduleController.class);

    private final ProviderScheduleService scheduleService;

    public ProviderScheduleController(ProviderScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @PostMapping("create-schedule")
    public ResponseEntity<ScheduleResponseDto> createSchedule(
            @Valid @RequestBody CreateScheduleRequestDto request) {
        log.info("Received request to create schedule for provider ID: {}", request.providerId());
        try {
            ScheduleResponseDto response = scheduleService.createSchedule(request);
            log.info("Schedule created successfully with ID: {}", response.id());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error creating schedule for provider ID: {}", request.providerId(), e);
            throw e;
        }
    }

    @GetMapping("/provider/{providerId}/available-slots")
    public ResponseEntity<List<TimeSlotResponseDto>> getAvailableSlots(
            @PathVariable Long providerId) {
        log.info("Received request to fetch available slots for provider ID: {}", providerId);
        try {
            List<TimeSlotResponseDto> slots = scheduleService.getAvailableSlots(providerId);
            log.info("Retrieved {} available slots for provider ID: {}", slots.size(), providerId);
            return ResponseEntity.ok(slots);
        } catch (Exception e) {
            log.error("Error fetching available slots for provider ID: {}", providerId, e);
            throw e;
        }
    }
}

