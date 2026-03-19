package com.dentist.booking.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "dentists")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Dentist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String qualification;

    private Integer experience;

    @Column(name = "clinic_name")
    private String clinicName;

    private String address;

    private String city;

    private String state;

    private String location;

    @Column(name = "location_link")
    private String locationLink;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "availability")
    private String availability = "Available";

    @Column(name = "working_days")
    private String workingDays = "MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY,SATURDAY";

    @Column(name = "working_start_time")
    private String workingStartTime = "09:00";

    @Column(name = "working_end_time")
    private String workingEndTime = "17:00";

    @Column(name = "appointment_duration")
    private Integer appointmentDuration = 30;

    @Column(name = "available_dates", columnDefinition = "TEXT")
    private String availableDates;

    @Column(name = "unavailable_dates", columnDefinition = "TEXT")
    private String unavailableDates;

    @Column(name = "morning_start")
    private String morningStart = "09:00";

    @Column(name = "morning_end")
    private String morningEnd = "13:00";

    @Column(name = "evening_start")
    private String eveningStart = "14:00";

    @Column(name = "evening_end")
    private String eveningEnd = "17:00";

    @Column(name = "morning_appointment_count")
    private Integer morningAppointmentCount = 8;

    @Column(name = "afternoon_appointment_count")
    private Integer afternoonAppointmentCount = 6;

    @Column(name = "upi_id")
    private String upiId;

    @Column(name = "photo_url")
    private String photoUrl;
}
