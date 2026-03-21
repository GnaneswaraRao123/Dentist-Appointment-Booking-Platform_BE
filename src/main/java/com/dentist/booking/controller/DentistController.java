package com.dentist.booking.controller;

import com.dentist.booking.dto.ApiResponse;
import com.dentist.booking.dto.DentistDTO;
import com.dentist.booking.dto.ProfileUpdateRequest;
import com.dentist.booking.entity.User;
import com.dentist.booking.exception.ResourceNotFoundException;
import com.dentist.booking.service.DentistService;
import com.dentist.booking.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "https://dentist-appointment-booking-platfor-one.vercel.app"})
public class DentistController {

    private final DentistService dentistService;
    private final UserService userService;

    @GetMapping("/dentists")
    public ResponseEntity<ApiResponse> getAllDentists(
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String search) {
        List<DentistDTO> dentists;
        if (search != null && !search.isEmpty()) {
            dentists = dentistService.searchDentists(search);
        } else if (location != null && !location.isEmpty()) {
            dentists = dentistService.getDentistsByLocation(location);
        } else {
            dentists = dentistService.getAllDentists();
        }
        return ResponseEntity.ok(ApiResponse.success("Dentists retrieved", dentists));
    }

    @GetMapping("/dentists/{id}")
    public ResponseEntity<ApiResponse> getDentistById(@PathVariable Long id) {
        DentistDTO dentist = dentistService.getDentistById(id);
        return ResponseEntity.ok(ApiResponse.success("Dentist retrieved", dentist));
    }

    @GetMapping("/dentists/my-profile")
    public ResponseEntity<ApiResponse> getMyDentistProfile() {
        User user = userService.getCurrentUser();
        try {
            DentistDTO dentist = dentistService.getOrCreateDentistByUserId(user.getId());
            return ResponseEntity.ok(ApiResponse.success("Profile retrieved", dentist));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.success("No dentist profile found", null));
        }
    }

    @PutMapping("/dentists/{id}")
    @PreAuthorize("hasRole('DENTIST') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> updateDentist(
            @PathVariable Long id,
            @Valid @RequestBody ProfileUpdateRequest request) {
        User currentUser = userService.getCurrentUser();
        String userRole = currentUser.getRole().name();
        
        if ("DENTIST".equals(userRole)) {
            try {
                DentistDTO existingDentist = dentistService.getDentistByUserId(currentUser.getId());
                if (existingDentist != null && !existingDentist.getId().equals(id)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(ApiResponse.error("You can only update your own dentist profile"));
                }
            } catch (Exception e) {
                dentistService.getOrCreateDentistByUserId(currentUser.getId());
            }
        }
        
        DentistDTO dentist = dentistService.updateDentist(id, request);
        return ResponseEntity.ok(ApiResponse.success("Dentist profile updated", dentist));
    }

    @PostMapping("/admin/dentists")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> createDentist(@RequestBody DentistDTO request) {
        DentistDTO dentist = dentistService.createDentist(request);
        return ResponseEntity.ok(ApiResponse.success("Dentist created", dentist));
    }

    @DeleteMapping("/admin/dentists/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> deleteDentist(@PathVariable Long id) {
        dentistService.deleteDentist(id);
        return ResponseEntity.ok(ApiResponse.success("Dentist deleted"));
    }

    @PutMapping("/admin/dentists/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> adminUpdateDentist(
            @PathVariable Long id,
            @Valid @RequestBody ProfileUpdateRequest request) {
        DentistDTO dentist = dentistService.updateDentistWithEmail(id, request);
        return ResponseEntity.ok(ApiResponse.success("Dentist updated successfully", dentist));
    }
}


