package com.dentist.booking.service;

import com.dentist.booking.dto.DentistDTO;
import com.dentist.booking.dto.ProfileUpdateRequest;
import com.dentist.booking.entity.Dentist;
import com.dentist.booking.entity.Role;
import com.dentist.booking.entity.User;
import com.dentist.booking.exception.BadRequestException;
import com.dentist.booking.exception.ResourceNotFoundException;
import com.dentist.booking.repository.DentistRepository;
import com.dentist.booking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DentistService {

    private final DentistRepository dentistRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<DentistDTO> getAllDentists() {
        return dentistRepository.findAllByOrderByIdDesc().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DentistDTO> getDentistsByLocation(String location) {
        return dentistRepository.findByLocation(location).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DentistDTO getDentistById(Long id) {
        Dentist dentist = dentistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dentist not found with id: " + id));
        return toDTO(dentist);
    }

    @Transactional(readOnly = true)
    public DentistDTO getDentistByUserId(Long userId) {
        Dentist dentist = dentistRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Dentist profile not found"));
        return toDTO(dentist);
    }

    @Transactional
    public DentistDTO getOrCreateDentistByUserId(Long userId) {
        return dentistRepository.findByUserId(userId)
                .map(this::toDTO)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
                    Dentist dentist = new Dentist();
                    dentist.setUser(user);
                    dentist.setAvailability("Available");
                    dentist = dentistRepository.save(dentist);
                    return toDTO(dentist);
                });
    }

    @Transactional
    public DentistDTO createDentist(DentistDTO dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new BadRequestException("Email already registered");
        }

        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode("dentist123"));
        user.setRole(Role.DENTIST);
        user.setPhone(dto.getPhone());
        user.setProfileCompleted(true);
        user = userRepository.save(user);

        Dentist dentist = new Dentist();
        dentist.setUser(user);
        dentist.setQualification(dto.getQualification());
        dentist.setExperience(dto.getExperience());
        dentist.setClinicName(dto.getClinicName());
        dentist.setAddress(dto.getAddress());
        dentist.setLocation(dto.getLocation());
        dentist.setPrice(dto.getPrice() != null ? dto.getPrice() : new BigDecimal("500.00"));
        dentist.setAvailability(dto.getAvailability() != null ? dto.getAvailability() : "Available");
        dentist.setPhotoUrl(dto.getPhotoUrl());

        dentist = dentistRepository.save(dentist);
        return toDTO(dentist);
    }

    @Transactional
    public DentistDTO updateDentist(Long id, ProfileUpdateRequest request) {
        Dentist dentist = dentistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dentist not found with id: " + id));

        updateDentistFields(dentist, request);

        dentist = dentistRepository.save(dentist);
        return toDTO(dentist);
    }

    private void updateDentistFields(Dentist dentist, ProfileUpdateRequest request) {
        User user = dentist.getUser();
        if (request.getName() != null) user.setName(request.getName());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getCity() != null) user.setCity(request.getCity());
        if (request.getState() != null) user.setState(request.getState());
        userRepository.save(user);

        if (request.getQualification() != null) dentist.setQualification(request.getQualification());
        if (request.getExperience() != null) dentist.setExperience(request.getExperience());
        if (request.getClinicName() != null) dentist.setClinicName(request.getClinicName());
        if (request.getAddress() != null) dentist.setAddress(request.getAddress());
        if (request.getCity() != null) dentist.setCity(request.getCity());
        if (request.getState() != null) dentist.setState(request.getState());
        if (request.getLocation() != null) dentist.setLocation(request.getLocation());
        if (request.getLocationLink() != null) dentist.setLocationLink(request.getLocationLink());
        if (request.getAvailability() != null) dentist.setAvailability(request.getAvailability());
        if (request.getPrice() != null) {
            dentist.setPrice(request.getPrice());
        }
        if (request.getAvailableDates() != null) dentist.setAvailableDates(request.getAvailableDates());
        if (request.getUnavailableDates() != null) dentist.setUnavailableDates(request.getUnavailableDates());
        if (request.getMorningStart() != null) dentist.setMorningStart(request.getMorningStart());
        if (request.getMorningEnd() != null) dentist.setMorningEnd(request.getMorningEnd());
        if (request.getEveningStart() != null) dentist.setEveningStart(request.getEveningStart());
        if (request.getEveningEnd() != null) dentist.setEveningEnd(request.getEveningEnd());
        if (request.getMorningAppointmentCount() != null) dentist.setMorningAppointmentCount(request.getMorningAppointmentCount());
        if (request.getAfternoonAppointmentCount() != null) dentist.setAfternoonAppointmentCount(request.getAfternoonAppointmentCount());
        if (request.getUpiId() != null) dentist.setUpiId(request.getUpiId());
    }

    @Transactional
    public DentistDTO updateDentistWithEmail(Long id, ProfileUpdateRequest request) {
        Dentist dentist = dentistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dentist not found with id: " + id));

        User user = dentist.getUser();
        if (request.getName() != null) user.setName(request.getName());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new BadRequestException("Email already in use");
            }
            user.setEmail(request.getEmail());
        }
        userRepository.save(user);

        if (request.getQualification() != null) dentist.setQualification(request.getQualification());
        if (request.getExperience() != null) dentist.setExperience(request.getExperience());
        if (request.getClinicName() != null) dentist.setClinicName(request.getClinicName());
        if (request.getAddress() != null) dentist.setAddress(request.getAddress());
        if (request.getLocation() != null) dentist.setLocation(request.getLocation());
        if (request.getAvailability() != null) dentist.setAvailability(request.getAvailability());
        if (request.getPrice() != null) {
            dentist.setPrice(request.getPrice());
        }
        if (request.getAvailableDates() != null) dentist.setAvailableDates(request.getAvailableDates());
        if (request.getUnavailableDates() != null) dentist.setUnavailableDates(request.getUnavailableDates());
        if (request.getMorningStart() != null) dentist.setMorningStart(request.getMorningStart());
        if (request.getMorningEnd() != null) dentist.setMorningEnd(request.getMorningEnd());
        if (request.getEveningStart() != null) dentist.setEveningStart(request.getEveningStart());
        if (request.getEveningEnd() != null) dentist.setEveningEnd(request.getEveningEnd());
        if (request.getMorningAppointmentCount() != null) dentist.setMorningAppointmentCount(request.getMorningAppointmentCount());
        if (request.getAfternoonAppointmentCount() != null) dentist.setAfternoonAppointmentCount(request.getAfternoonAppointmentCount());
        if (request.getUpiId() != null) dentist.setUpiId(request.getUpiId());

        dentist = dentistRepository.save(dentist);
        return toDTO(dentist);
    }

    @Transactional
    public void deleteDentist(Long id) {
        Dentist dentist = dentistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dentist not found with id: " + id));
        userRepository.delete(dentist.getUser());
        dentistRepository.delete(dentist);
    }

    @Transactional(readOnly = true)
    public List<DentistDTO> searchDentists(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllDentists();
        }
        return dentistRepository.findByLocation(query).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private DentistDTO toDTO(Dentist dentist) {
        DentistDTO dto = new DentistDTO();
        dto.setId(dentist.getId());
        dto.setUserId(dentist.getUser().getId());
        dto.setName(dentist.getUser().getName());
        dto.setEmail(dentist.getUser().getEmail());
        dto.setPhone(dentist.getUser().getPhone());
        dto.setCity(dentist.getCity() != null ? dentist.getCity() : dentist.getUser().getCity());
        dto.setState(dentist.getState() != null ? dentist.getState() : dentist.getUser().getState());
        dto.setQualification(dentist.getQualification());
        dto.setExperience(dentist.getExperience());
        dto.setClinicName(dentist.getClinicName());
        dto.setAddress(dentist.getAddress());
        dto.setLocation(dentist.getLocation());
        dto.setLocationLink(dentist.getLocationLink());
        dto.setPrice(dentist.getPrice());
        dto.setAvailability(dentist.getAvailability());
        dto.setAvailableDates(dentist.getAvailableDates());
        dto.setUnavailableDates(dentist.getUnavailableDates());
        dto.setMorningStart(dentist.getMorningStart());
        dto.setMorningEnd(dentist.getMorningEnd());
        dto.setEveningStart(dentist.getEveningStart());
        dto.setEveningEnd(dentist.getEveningEnd());
        dto.setMorningAppointmentCount(dentist.getMorningAppointmentCount());
        dto.setAfternoonAppointmentCount(dentist.getAfternoonAppointmentCount());
        dto.setUpiId(dentist.getUpiId());
        dto.setPhotoUrl(dentist.getPhotoUrl());
        return dto;
    }
}
