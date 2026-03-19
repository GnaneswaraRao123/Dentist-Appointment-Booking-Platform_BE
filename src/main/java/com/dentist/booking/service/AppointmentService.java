package com.dentist.booking.service;

import com.dentist.booking.dto.AppointmentDTO;
import com.dentist.booking.dto.AppointmentRequest;
import com.dentist.booking.entity.*;
import com.dentist.booking.exception.BadRequestException;
import com.dentist.booking.exception.ProfileIncompleteException;
import com.dentist.booking.exception.ResourceNotFoundException;
import com.dentist.booking.repository.AppointmentRepository;
import com.dentist.booking.repository.DentistRepository;
import com.dentist.booking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private static final Logger log = LoggerFactory.getLogger(AppointmentService.class);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    
    private final AppointmentRepository appointmentRepository;
    private final DentistRepository dentistRepository;
    private final UserRepository userRepository;

    @Transactional
    public AppointmentDTO createAppointment(AppointmentRequest request) {
        User user = getCurrentUser();
        
        if (!user.isProfileCompleted()) {
            throw new ProfileIncompleteException("Please complete your profile first before booking an appointment");
        }

        Dentist dentist = dentistRepository.findById(request.getDentistId())
                .orElseThrow(() -> new ResourceNotFoundException("Dentist not found"));

        SessionType sessionType = SessionType.valueOf(request.getSessionType().toUpperCase());
        
        int bookedCount = appointmentRepository.countByDentistIdAndDateAndSessionType(
                request.getDentistId(), request.getDate(), sessionType);
        
        int maxAppointments = sessionType == SessionType.MORNING 
                ? dentist.getMorningAppointmentCount() 
                : dentist.getAfternoonAppointmentCount();
        
        if (bookedCount >= maxAppointments) {
            throw new BadRequestException("No more appointments available for this session. Please select a different session or date.");
        }

        if (appointmentRepository.existsByDentistIdAndDateAndTime(
                request.getDentistId(), request.getDate(), request.getTime())) {
            throw new BadRequestException("This time slot is already booked. Please select a different time.");
        }

        Appointment appointment = new Appointment();
        appointment.setPatientName(request.getPatientName());
        appointment.setAge(request.getAge());
        appointment.setGender(request.getGender());
        appointment.setDate(request.getDate());
        appointment.setTime(request.getTime());
        appointment.setStatus(AppointmentStatus.BOOKED);
        appointment.setSessionType(sessionType);
        appointment.setPaymentStatus(PaymentStatus.PENDING);
        appointment.setDentist(dentist);
        appointment.setUser(user);
        appointment.setAmount(dentist.getPrice());
        appointment.setPaymentRemarks(request.getPaymentRemarks());
        
        String uniqueUtr = "UTR" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        appointment.setUtrNumber(uniqueUtr);
        appointment.setUtrCreatedAt(LocalDateTime.now());

        appointment = appointmentRepository.save(appointment);
        return toDTO(appointment);
    }

    @Transactional
    public AppointmentDTO regenerateUtr(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));
        
        if (appointment.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new BadRequestException("Cannot regenerate UTR for already paid or cancelled appointments");
        }
        
        String newUtr = "UTR" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        appointment.setUtrNumber(newUtr);
        appointment.setUtrCreatedAt(LocalDateTime.now());
        
        appointment = appointmentRepository.save(appointment);
        return toDTO(appointment);
    }

    public List<AppointmentDTO> getAppointmentsForUser(Long userId) {
        return appointmentRepository.findByUserIdOrderByIdDesc(userId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<AppointmentDTO> getAppointmentsForDentist(Long userId) {
        return appointmentRepository.findByDentistUserIdOrderByIdDesc(userId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<AppointmentDTO> getAllAppointments() {
        return appointmentRepository.findAllByOrderByIdDesc().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public AppointmentDTO getAppointmentById(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));
        return toDTO(appointment);
    }

    @Transactional
    public AppointmentDTO updateAppointmentStatus(Long id, AppointmentStatus status) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        appointment.setStatus(status);
        appointment = appointmentRepository.save(appointment);
        return toDTO(appointment);
    }

    @Transactional
    public AppointmentDTO updatePaymentStatus(Long id, PaymentStatus paymentStatus) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        appointment.setPaymentStatus(paymentStatus);
        appointment = appointmentRepository.save(appointment);
        return toDTO(appointment);
    }

    public List<String> getBookedSlots(Long dentistId, LocalDate date, SessionType sessionType) {
        return appointmentRepository.findByDentistIdAndDateAndSessionType(dentistId, date, sessionType).stream()
                .map(Appointment::getTime)
                .map(time -> time.format(TIME_FORMATTER))
                .collect(Collectors.toList());
    }
    
    public int getMorningBookedCount(Long dentistId, LocalDate date) {
        return appointmentRepository.countByDentistIdAndDateAndSessionType(dentistId, date, SessionType.MORNING);
    }
    
    public int getAfternoonBookedCount(Long dentistId, LocalDate date) {
        return appointmentRepository.countByDentistIdAndDateAndSessionType(dentistId, date, SessionType.AFTERNOON);
    }

    public List<AppointmentDTO> getAppointmentsByStatus(AppointmentStatus status) {
        return appointmentRepository.findByStatusOrderByIdDesc(status).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteAppointment(Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String role = auth.getAuthorities().stream().findFirst().map(a -> a.getAuthority()).orElse("");
        Long userId = Long.parseLong(auth.getName());
        
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));
        
        boolean isAdmin = role.equals("ROLE_ADMIN");
        boolean isOwner = appointment.getUser() != null && appointment.getUser().getId().equals(userId);
        boolean isDentist = appointment.getDentist() != null && appointment.getDentist().getUser() != null 
                && appointment.getDentist().getUser().getId().equals(userId);
        
        if (!isAdmin && !isOwner && !isDentist) {
            throw new ResourceNotFoundException("Access denied");
        }
        
        if (!isAdmin) {
            String statusName = appointment.getStatus().name();
            LocalDate apptDate = appointment.getDate();
            LocalDate today = java.time.LocalDate.now();
            boolean isRejected = "REJECTED".equals(statusName);
            boolean isPast = apptDate.isBefore(today);
            
            log.info("Delete check - id: {}, status: {}, apptDate: {}, today: {}, isRejected: {}, isPast: {}", 
                id, statusName, apptDate, today, isRejected, isPast);
            
            if (!isRejected && !isPast) {
                log.warn("Delete denied - isRejected: {}, isPast: {}", isRejected, isPast);
                throw new BadRequestException("Cannot delete active or upcoming appointments");
            }
        }
        
        appointmentRepository.delete(appointment);
    }

    @Transactional
    public void cancelAppointment(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));
        appointmentRepository.delete(appointment);
    }

    @Transactional
    public AppointmentDTO verifyAndConfirmPayment(Long id, String enteredUtr) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));
        
        if (appointment.getUtrNumber() == null) {
            throw new BadRequestException("No UTR number generated for this appointment");
        }
        
        if (!appointment.getUtrNumber().equals(enteredUtr.trim())) {
            throw new BadRequestException("UTR number does not match. Please check and enter the correct UTR.");
        }
        
        appointment.setPaymentStatus(PaymentStatus.SUCCESS);
        appointment = appointmentRepository.save(appointment);
        return toDTO(appointment);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private AppointmentDTO toDTO(Appointment appointment) {
        AppointmentDTO dto = new AppointmentDTO();
        dto.setId(appointment.getId());
        dto.setPatientName(appointment.getPatientName());
        dto.setAge(appointment.getAge());
        dto.setGender(appointment.getGender() != null ? appointment.getGender().name() : null);
        dto.setDate(appointment.getDate());
        dto.setTime(appointment.getTime());
        dto.setStatus(appointment.getStatus().name());
        dto.setSessionType(appointment.getSessionType() != null ? appointment.getSessionType().name() : null);
        dto.setPaymentStatus(appointment.getPaymentStatus().name());
        dto.setDentistId(appointment.getDentist().getId());
        dto.setDentistName(appointment.getDentist().getUser().getName());
        dto.setClinicName(appointment.getDentist().getClinicName());
        dto.setDentistPhone(appointment.getDentist().getUser().getPhone());
        dto.setDentistAddress(appointment.getDentist().getAddress());
        dto.setDentistLocation(appointment.getDentist().getLocation());
        dto.setDentistLocationLink(appointment.getDentist().getLocationLink());
        dto.setUserId(appointment.getUser().getId());
        dto.setUserName(appointment.getUser().getName());
        dto.setAmount(appointment.getAmount());
        dto.setUtrNumber(appointment.getUtrNumber());
        dto.setUtrCreatedAt(appointment.getUtrCreatedAt());
        dto.setPaymentRemarks(appointment.getPaymentRemarks());
        return dto;
    }
}
