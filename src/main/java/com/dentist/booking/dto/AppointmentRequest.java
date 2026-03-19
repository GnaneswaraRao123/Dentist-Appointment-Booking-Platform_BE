package com.dentist.booking.dto;

import com.dentist.booking.entity.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class AppointmentRequest {
    @NotBlank(message = "Patient name is required")
    private String patientName;

    @NotNull(message = "Age is required")
    private Integer age;

    @NotNull(message = "Gender is required")
    private Gender gender;

    @NotNull(message = "Date is required")
    private LocalDate date;

    @NotNull(message = "Time is required")
    private LocalTime time;

    @NotNull(message = "Dentist ID is required")
    private Long dentistId;
    
    private String sessionType;
    private String paymentRemarks;
}
