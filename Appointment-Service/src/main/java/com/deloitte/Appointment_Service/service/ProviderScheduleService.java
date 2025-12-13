package com.deloitte.Appointment_Service.service;

import com.deloitte.Appointment_Service.dto.CreateScheduleRequestDto;
import com.deloitte.Appointment_Service.dto.ScheduleResponseDto;
import com.deloitte.Appointment_Service.dto.TimeSlotResponseDto;
import com.deloitte.Appointment_Service.exception.ProviderTimeSlotsNotFoundException;
import com.deloitte.Appointment_Service.model.ProviderTimeSlots;
import com.deloitte.Appointment_Service.repository.ProviderTimeSlotsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProviderScheduleService {

    private static final Logger log = LoggerFactory.getLogger(ProviderScheduleService.class);

    private final ProviderTimeSlotsRepository timeSlotsRepository;

    public ProviderScheduleService(ProviderTimeSlotsRepository timeSlotsRepository) {
        this.timeSlotsRepository = timeSlotsRepository;
    }

    @Transactional
    public ScheduleResponseDto createSchedule(CreateScheduleRequestDto request) {
        log.info("Creating schedule for provider ID: {} on date: {}", 
                request.providerId(), request.scheduleDate());

        // Check if schedule already exists for this provider and date
        Optional<ProviderTimeSlots> existing = timeSlotsRepository
                .findByProviderIdAndScheduleDate(request.providerId(), request.scheduleDate());
        
        if (existing.isPresent()) {
            throw new IllegalArgumentException(
                    "Schedule already exists for provider " + request.providerId() + 
                    " on date " + request.scheduleDate());
        }

        // Create the provider time slots
        ProviderTimeSlots timeSlots = ProviderTimeSlots.builder()
                .providerId(request.providerId())
                .scheduleDate(request.scheduleDate())
                .slots(request.slots())
                .build();

        timeSlots = timeSlotsRepository.save(timeSlots);
        log.info("Schedule created with ID: {} containing {} time slots", 
                timeSlots.getId(), request.slots().size());

        return mapToScheduleResponseDto(timeSlots, "Schedule created successfully");
    }

    public List<TimeSlotResponseDto> getAvailableSlots(Long providerId) {
        log.info("Fetching available slots for provider ID: {}", providerId);
        
        LocalDate today = LocalDate.now();
        List<ProviderTimeSlots> providerSlots = timeSlotsRepository
                .findByProviderIdAndScheduleDateGreaterThanEqual(providerId, today);
        
        if (providerSlots.isEmpty()) {
            log.info("No schedules found for provider ID: {}", providerId);
            return Collections.emptyList();
        }

        List<TimeSlotResponseDto> availableSlots = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (ProviderTimeSlots pts : providerSlots) {
            for (Map.Entry<String, Boolean> entry : pts.getSlots().entrySet()) {
                try {
                    LocalDateTime slotTime;
                    String slotKey = entry.getKey();
                    
                    // Try to parse as full datetime first
                    if (slotKey.contains("T")) {
                        slotTime = LocalDateTime.parse(slotKey);
                    } else {
                        // If it's just time (HH:mm:ss), combine with schedule date
                        slotTime = LocalDateTime.of(pts.getScheduleDate(), 
                                java.time.LocalTime.parse(slotKey));
                    }
                    
                    Boolean isAvailable = entry.getValue();
                    
                    // Only include future slots that are available
                    if (slotTime.isAfter(now) && isAvailable) {
                        availableSlots.add(new TimeSlotResponseDto(slotTime, isAvailable));
                    }
                } catch (Exception e) {
                    log.error("Failed to parse slot time: {}", entry.getKey(), e);
                }
            }
        }

        // Sort by time
        availableSlots.sort(Comparator.comparing(TimeSlotResponseDto::slotTime));
        
        log.info("Found {} available slots for provider ID: {}", availableSlots.size(), providerId);
        
        return availableSlots;
    }

    @Transactional
    public void updateSlotAvailability(Long providerId, LocalDateTime slotTime, Boolean isAvailable) {
        log.info("Updating slot availability for provider ID: {} at time: {} to: {}", 
                providerId, slotTime, isAvailable);

        LocalDate scheduleDate = slotTime.toLocalDate();
        ProviderTimeSlots timeSlots = timeSlotsRepository
                .findByProviderIdAndScheduleDate(providerId, scheduleDate)
                .orElseThrow(() -> new ProviderTimeSlotsNotFoundException(
                        "No schedule found for provider " + providerId + " on date " + scheduleDate));

        // Extract just the time portion (HH:mm:ss) to match database format
        String slotKey = slotTime.toLocalTime().toString();
        if (!timeSlots.getSlots().containsKey(slotKey)) {
            throw new IllegalArgumentException(
                    "Slot time " + slotTime + " does not exist in the schedule");
        }

        // Update the slot availability
        timeSlots.getSlots().put(slotKey, isAvailable);
        timeSlotsRepository.save(timeSlots);
        
        log.info("Slot availability updated successfully");
    }

    public boolean isSlotAvailable(Long providerId, LocalDateTime slotTime) {
        LocalDate scheduleDate = slotTime.toLocalDate();
        Optional<ProviderTimeSlots> timeSlotsOpt = timeSlotsRepository
                .findByProviderIdAndScheduleDate(providerId, scheduleDate);

        if (timeSlotsOpt.isEmpty()) {
            return false;
        }

        // Extract just the time portion (HH:mm:ss) to match database format
        String slotKey = slotTime.toLocalTime().toString();
        Map<String, Boolean> slots = timeSlotsOpt.get().getSlots();
        
        return slots.containsKey(slotKey) && slots.get(slotKey);
    }

    private ScheduleResponseDto mapToScheduleResponseDto(ProviderTimeSlots timeSlots, String message) {
        return new ScheduleResponseDto(
                timeSlots.getId(),
                timeSlots.getProviderId(),
                timeSlots.getScheduleDate(),
                timeSlots.getSlots(),
                message,
                null
        );
    }
}
