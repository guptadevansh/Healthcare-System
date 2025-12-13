package com.deloitte.Appointment_Service.repository;

import com.deloitte.Appointment_Service.model.ProviderTimeSlots;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProviderTimeSlotsRepository extends JpaRepository<ProviderTimeSlots, Long> {

    Optional<ProviderTimeSlots> findByProviderIdAndScheduleDate(Long providerId, LocalDate scheduleDate);
    
    List<ProviderTimeSlots> findByProviderIdAndScheduleDateGreaterThanEqual(
            Long providerId, LocalDate startDate);
}

