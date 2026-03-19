package com.dentist.booking.repository;

import com.dentist.booking.entity.Appointment;
import com.dentist.booking.entity.AppointmentStatus;
import com.dentist.booking.entity.Dentist;
import com.dentist.booking.entity.SessionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByUserIdOrderByIdDesc(Long userId);
    List<Appointment> findByDentistIdOrderByIdDesc(Long dentistId);
    List<Appointment> findByDentistUserIdOrderByIdDesc(Long userId);
    List<Appointment> findByStatusOrderByIdDesc(AppointmentStatus status);
    List<Appointment> findAllByOrderByIdDesc();
    
    @Query("SELECT a FROM Appointment a WHERE a.dentist.id = :dentistId AND a.date = :date AND a.time = :time")
    List<Appointment> findByDentistIdAndDateAndTime(
            @Param("dentistId") Long dentistId, 
            @Param("date") LocalDate date, 
            @Param("time") LocalTime time);
    
    boolean existsByDentistIdAndDateAndTime(Long dentistId, LocalDate date, LocalTime time);
    
    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.dentist.id = :dentistId AND a.date = :date AND a.sessionType = :sessionType")
    int countByDentistIdAndDateAndSessionType(
            @Param("dentistId") Long dentistId, 
            @Param("date") LocalDate date,
            @Param("sessionType") SessionType sessionType);
    
    @Query("SELECT a FROM Appointment a WHERE a.dentist.id = :dentistId AND a.date = :date AND a.sessionType = :sessionType")
    List<Appointment> findByDentistIdAndDateAndSessionType(
            @Param("dentistId") Long dentistId,
            @Param("date") LocalDate date,
            @Param("sessionType") SessionType sessionType);
}
