package com.dentist.booking.dto;

import com.dentist.booking.entity.PaymentStatus;
import lombok.Data;

@Data
public class PaymentStatusUpdate {
    private PaymentStatus status;
}
