package com.dentist.booking.controller;

import com.dentist.booking.dto.ApiResponse;
import com.dentist.booking.dto.PaymentRequest;
import com.dentist.booking.entity.Payment;
import com.dentist.booking.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<ApiResponse> processPayment(@Valid @RequestBody PaymentRequest request) {
        Payment payment = paymentService.processPayment(request);
        return ResponseEntity.ok(ApiResponse.success("Payment submitted for verification", payment));
    }

    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<ApiResponse> getPaymentByAppointment(@PathVariable Long appointmentId) {
        Payment payment = paymentService.getPaymentByAppointmentId(appointmentId);
        return ResponseEntity.ok(ApiResponse.success("Payment retrieved", payment));
    }

    @GetMapping("/generate-utr")
    public ResponseEntity<ApiResponse> generateUtr() {
        String utr = paymentService.generateUtrNumber();
        Map<String, String> response = new HashMap<>();
        response.put("utrNumber", utr);
        return ResponseEntity.ok(ApiResponse.success("UTR generated", response));
    }

    @PutMapping("/{paymentId}/confirm")
    @PreAuthorize("hasRole('DENTIST') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> confirmPayment(@PathVariable Long paymentId) {
        Payment payment = paymentService.confirmPayment(paymentId);
        return ResponseEntity.ok(ApiResponse.success("Payment confirmed", payment));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> getAllPayments(
            @RequestParam(required = false) String status) {
        List<Payment> payments;
        if (status != null && !status.isEmpty()) {
            payments = paymentService.getPaymentsByStatus(status);
        } else {
            payments = paymentService.getAllPayments();
        }
        return ResponseEntity.ok(ApiResponse.success("Payments retrieved", payments));
    }
}
