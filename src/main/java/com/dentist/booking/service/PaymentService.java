package com.dentist.booking.service;

import com.dentist.booking.dto.PaymentRequest;
import com.dentist.booking.entity.Appointment;
import com.dentist.booking.entity.Payment;
import com.dentist.booking.entity.PaymentStatus;
import com.dentist.booking.exception.BadRequestException;
import com.dentist.booking.exception.ResourceNotFoundException;
import com.dentist.booking.repository.AppointmentRepository;
import com.dentist.booking.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final AppointmentRepository appointmentRepository;

    @Transactional
    public Payment processPayment(PaymentRequest request) {
        Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        if (paymentRepository.findByAppointmentId(request.getAppointmentId()).isPresent()) {
            throw new BadRequestException("Payment already processed for this appointment");
        }

        Payment payment = new Payment();
        payment.setAppointment(appointment);
        payment.setAmount(appointment.getAmount());
        payment.setPaymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : "UPI");
        payment.setStatus(PaymentStatus.PENDING);
        payment.setUtrNumber(request.getUtrNumber());
        payment.setUpiId(request.getUpiId());
        payment.setPaymentRemarks(request.getPaymentRemarks());

        payment = paymentRepository.save(payment);
        return payment;
    }

    @Transactional
    public Payment confirmPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
        payment.setStatus(PaymentStatus.SUCCESS);
        return paymentRepository.save(payment);
    }

    public Payment getPaymentByAppointmentId(Long appointmentId) {
        return paymentRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
    }

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    public List<Payment> getPaymentsByStatus(String status) {
        return paymentRepository.findByStatus(status);
    }
    
    public String generateUtrNumber() {
        return "UTR" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }
}
