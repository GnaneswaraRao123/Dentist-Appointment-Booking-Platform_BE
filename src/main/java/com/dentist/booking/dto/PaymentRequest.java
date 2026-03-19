package com.dentist.booking.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentRequest {
    @NotNull(message = "Appointment ID is required")
    private Long appointmentId;

    private String paymentMethod;
    private String utrNumber;
    private String upiId;
    private String paymentRemarks;
}
