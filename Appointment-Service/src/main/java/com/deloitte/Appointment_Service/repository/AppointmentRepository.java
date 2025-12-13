package com.deloitte.Appointment_Service.repository;

import com.deloitte.Appointment_Service.constants.AppointmentStatus;
import com.deloitte.Appointment_Service.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    
    List<Appointment> findByPatientId(Long patientId);
    
    List<Appointment> findByProviderId(Long providerId);
    
    List<Appointment> findByProviderIdAndStatus(Long providerId, AppointmentStatus status);
    
    List<Appointment> findByPatientIdAndStatus(Long patientId, AppointmentStatus status);
}

