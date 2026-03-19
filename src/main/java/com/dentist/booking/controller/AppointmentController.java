package com.dentist.booking.controller;

import com.dentist.booking.dto.ApiResponse;
import com.dentist.booking.dto.AppointmentDTO;
import com.dentist.booking.dto.AppointmentRequest;
import com.dentist.booking.dto.AppointmentStatusUpdate;
import com.dentist.booking.dto.PaymentStatusUpdate;
import com.dentist.booking.entity.AppointmentStatus;
import com.dentist.booking.entity.SessionType;
import com.dentist.booking.entity.User;
import com.dentist.booking.service.AppointmentService;
import com.dentist.booking.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final UserService userService;

    @PostMapping("/appointments")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse> createAppointment(@Valid @RequestBody AppointmentRequest request) {
        AppointmentDTO appointment = appointmentService.createAppointment(request);
        return ResponseEntity.ok(ApiResponse.success("Appointment booked successfully", appointment));
    }

    @GetMapping("/appointments/my")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse> getMyAppointments() {
        User user = userService.getCurrentUser();
        List<AppointmentDTO> appointments = appointmentService.getAppointmentsForUser(user.getId());
        return ResponseEntity.ok(ApiResponse.success("Appointments retrieved", appointments));
    }

    @GetMapping("/appointments/dentist")
    @PreAuthorize("hasRole('DENTIST')")
    public ResponseEntity<ApiResponse> getDentistAppointments() {
        User user = userService.getCurrentUser();
        List<AppointmentDTO> appointments = appointmentService.getAppointmentsForDentist(user.getId());
        return ResponseEntity.ok(ApiResponse.success("Appointments retrieved", appointments));
    }

    @GetMapping("/appointments")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> getAllAppointments(
            @RequestParam(required = false) String status) {
        List<AppointmentDTO> appointments;
        if (status != null && !status.isEmpty()) {
            appointments = appointmentService.getAppointmentsByStatus(AppointmentStatus.valueOf(status.toUpperCase()));
        } else {
            appointments = appointmentService.getAllAppointments();
        }
        return ResponseEntity.ok(ApiResponse.success("Appointments retrieved", appointments));
    }

    @GetMapping("/appointments/{id}")
    public ResponseEntity<ApiResponse> getAppointmentById(@PathVariable Long id) {
        AppointmentDTO appointment = appointmentService.getAppointmentById(id);
        return ResponseEntity.ok(ApiResponse.success("Appointment retrieved", appointment));
    }

    @PutMapping("/appointments/{id}/status")
    @PreAuthorize("hasRole('DENTIST') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> updateAppointmentStatus(
            @PathVariable Long id,
            @RequestBody AppointmentStatusUpdate request) {
        AppointmentDTO appointment = appointmentService.updateAppointmentStatus(id, request.getStatus());
        return ResponseEntity.ok(ApiResponse.success("Appointment status updated", appointment));
    }

    @PutMapping("/appointments/{id}/payment-status")
    @PreAuthorize("hasRole('DENTIST') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> updatePaymentStatus(
            @PathVariable Long id,
            @RequestBody PaymentStatusUpdate request) {
        AppointmentDTO appointment = appointmentService.updatePaymentStatus(id, request.getStatus());
        return ResponseEntity.ok(ApiResponse.success("Payment status updated", appointment));
    }

    @GetMapping("/appointments/{dentistId}/slots")
    public ResponseEntity<ApiResponse> getBookedSlots(
            @PathVariable Long dentistId,
            @RequestParam LocalDate date,
            @RequestParam String sessionType) {
        SessionType session = SessionType.valueOf(sessionType.toUpperCase());
        List<String> bookedSlots = appointmentService.getBookedSlots(dentistId, date, session);
        return ResponseEntity.ok(ApiResponse.success("Booked slots retrieved", bookedSlots));
    }
    
    @GetMapping("/appointments/{dentistId}/availability")
    public ResponseEntity<ApiResponse> getAvailability(
            @PathVariable Long dentistId,
            @RequestParam LocalDate date) {
        Map<String, Object> availability = new HashMap<>();
        availability.put("morningBooked", appointmentService.getMorningBookedCount(dentistId, date));
        availability.put("afternoonBooked", appointmentService.getAfternoonBookedCount(dentistId, date));
        return ResponseEntity.ok(ApiResponse.success("Availability retrieved", availability));
    }

    @DeleteMapping("/appointments/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DENTIST', 'CUSTOMER')")
    public ResponseEntity<ApiResponse> deleteAppointment(@PathVariable Long id) {
        appointmentService.deleteAppointment(id);
        return ResponseEntity.ok(ApiResponse.success("Appointment deleted"));
    }

    @PostMapping("/appointments/{id}/verify-payment")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse> verifyPayment(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        String enteredUtr = request.get("utrNumber");
        AppointmentDTO appointment = appointmentService.verifyAndConfirmPayment(id, enteredUtr);
        return ResponseEntity.ok(ApiResponse.success("Payment verified and confirmed successfully!", appointment));
    }

    @PostMapping("/appointments/{id}/cancel")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse> cancelAppointment(@PathVariable Long id) {
        appointmentService.cancelAppointment(id);
        return ResponseEntity.ok(ApiResponse.success("Appointment cancelled successfully"));
    }

    @PostMapping("/appointments/{id}/regenerate-utr")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse> regenerateUtr(@PathVariable Long id) {
        AppointmentDTO appointment = appointmentService.regenerateUtr(id);
        return ResponseEntity.ok(ApiResponse.success("New UTR generated successfully", appointment));
    }
}
