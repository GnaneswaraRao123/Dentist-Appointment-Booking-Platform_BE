package com.dentist.booking.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProfileUpdateRequest {
    private String name;
    private String email;
    private String phone;
    private String city;
    private String state;
    private String qualification;
    private Integer experience;
    private String clinicName;
    private String address;
    private String location;
    private String locationLink;
    private String availability;
    private BigDecimal price;
    private String availableDates;
    private String unavailableDates;
    private String morningStart;
    private String morningEnd;
    private String eveningStart;
    private String eveningEnd;
    private Integer morningAppointmentCount;
    private Integer afternoonAppointmentCount;
    private String upiId;
}
