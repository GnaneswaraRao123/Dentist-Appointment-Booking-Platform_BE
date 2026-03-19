package com.dentist.booking.service;

import com.dentist.booking.dto.AdminUserUpdateRequest;
import com.dentist.booking.dto.ProfileUpdateRequest;
import com.dentist.booking.entity.Role;
import com.dentist.booking.entity.User;
import com.dentist.booking.exception.BadRequestException;
import com.dentist.booking.exception.ResourceNotFoundException;
import com.dentist.booking.repository.AppointmentRepository;
import com.dentist.booking.repository.DentistRepository;
import com.dentist.booking.repository.PaymentRepository;
import com.dentist.booking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final DentistRepository dentistRepository;
    private final AppointmentRepository appointmentRepository;
    private final PaymentRepository paymentRepository;
    private final PasswordEncoder passwordEncoder;

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    public List<User> getAllUsers() {
        return userRepository.findAllOrderByRoleThenId();
    }

    @Transactional
    public User updateProfile(ProfileUpdateRequest request) {
        User user = getCurrentUser();
        
        if (request.getName() != null) user.setName(request.getName());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getCity() != null) user.setCity(request.getCity());
        if (request.getState() != null) user.setState(request.getState());
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new BadRequestException("Email already in use");
            }
            user.setEmail(request.getEmail());
        }
        
        boolean profileCompleted = user.getName() != null && !user.getName().isEmpty()
                && user.getPhone() != null && !user.getPhone().isEmpty();
        user.setProfileCompleted(profileCompleted);

        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        if (user.getRole() == Role.DENTIST) {
            var dentistOpt = dentistRepository.findByUserId(id);
            if (dentistOpt.isPresent()) {
                var dentist = dentistOpt.get();
                var appointments = appointmentRepository.findByDentistIdOrderByIdDesc(dentist.getId());
                for (var apt : appointments) {
                    var payment = apt.getPayment();
                    apt.setPayment(null);
                    appointmentRepository.save(apt);
                    if (payment != null) {
                        paymentRepository.delete(payment);
                    }
                }
                appointmentRepository.deleteAll(appointments);
                dentistRepository.delete(dentist);
            }
        } else {
            var appointments = appointmentRepository.findByUserIdOrderByIdDesc(id);
            for (var apt : appointments) {
                var payment = apt.getPayment();
                apt.setPayment(null);
                appointmentRepository.save(apt);
                if (payment != null) {
                    paymentRepository.delete(payment);
                }
            }
            appointmentRepository.deleteAll(appointments);
        }
        
        userRepository.delete(user);
    }

    @Transactional
    public User updateUserByAdmin(Long id, AdminUserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        if (request.getName() != null) user.setName(request.getName());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new BadRequestException("Email already in use");
            }
            user.setEmail(request.getEmail());
        }
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getProfileCompleted() != null) {
            user.setProfileCompleted(request.getProfileCompleted());
        }
        
        return userRepository.save(user);
    }
}
