package com.dentist.booking.dto;

import com.dentist.booking.entity.AppointmentStatus;
import lombok.Data;

@Data
public class AppointmentStatusUpdate {
    private AppointmentStatus status;
}
