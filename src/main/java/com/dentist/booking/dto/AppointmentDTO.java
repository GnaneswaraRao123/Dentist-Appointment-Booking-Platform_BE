package com.dentist.booking.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class AppointmentDTO {
    private Long id;
    private String patientName;
    private Integer age;
    private String gender;
    private LocalDate date;
    private LocalTime time;
    private String status;
    private Long dentistId;
    private String dentistName;
    private String clinicName;
    private String dentistPhone;
    private String dentistAddress;
    private String dentistLocation;
    private String dentistLocationLink;
    private Long userId;
    private String userName;
    private BigDecimal amount;
    private String paymentStatus;
    private String sessionType;
    private String utrNumber;
    private LocalDateTime utrCreatedAt;
    private String paymentRemarks;
}
