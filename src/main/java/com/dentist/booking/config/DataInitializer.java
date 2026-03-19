package com.dentist.booking.config;

import com.dentist.booking.entity.*;
import com.dentist.booking.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final DentistRepository dentistRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            initializeData();
        }
    }

    private void initializeData() {
        User admin = new User();
        admin.setName("Admin User");
        admin.setEmail("admin@gmail.com");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRole(Role.ADMIN);
        admin.setProfileCompleted(true);
        userRepository.save(admin);

        User customer = new User();
        customer.setName("John Doe");
        customer.setEmail("user@gmail.com");
        customer.setPassword(passwordEncoder.encode("user123"));
        customer.setRole(Role.CUSTOMER);
        customer.setPhone("1234567890");
        customer.setProfileCompleted(true);
        userRepository.save(customer);

        User dentistUser = new User();
        dentistUser.setName("Dr. Sarah Smith");
        dentistUser.setEmail("dentist1@gmail.com");
        dentistUser.setPassword(passwordEncoder.encode("dentist123"));
        dentistUser.setRole(Role.DENTIST);
        dentistUser.setPhone("9876543210");
        dentistUser.setProfileCompleted(true);
        userRepository.save(dentistUser);

        Dentist dentist1 = new Dentist();
        dentist1.setUser(dentistUser);
        dentist1.setQualification("BDS, MDS");
        dentist1.setExperience(10);
        dentist1.setClinicName("Smile Dental Clinic");
        dentist1.setAddress("123 Main Street");
        dentist1.setLocation("New York");
        dentist1.setPrice(new BigDecimal("500.00"));
        dentist1.setAvailability("Mon-Sat, 9AM-5PM");
        dentist1.setPhotoUrl(null);
        dentistRepository.save(dentist1);

        User dentistUser2 = new User();
        dentistUser2.setName("Dr. Michael Chen");
        dentistUser2.setEmail("dentist2@gmail.com");
        dentistUser2.setPassword(passwordEncoder.encode("dentist123"));
        dentistUser2.setRole(Role.DENTIST);
        dentistUser2.setPhone("5555555555");
        dentistUser2.setProfileCompleted(true);
        userRepository.save(dentistUser2);

        Dentist dentist2 = new Dentist();
        dentist2.setUser(dentistUser2);
        dentist2.setQualification("BDS, FICOI");
        dentist2.setExperience(15);
        dentist2.setClinicName("Perfect Smiles Dental");
        dentist2.setAddress("456 Oak Avenue");
        dentist2.setLocation("Los Angeles");
        dentist2.setPrice(new BigDecimal("750.00"));
        dentist2.setAvailability("Mon-Fri, 10AM-6PM");
        dentist2.setPhotoUrl(null);
        dentistRepository.save(dentist2);

        System.out.println("Demo data initialized successfully!");
    }
}
