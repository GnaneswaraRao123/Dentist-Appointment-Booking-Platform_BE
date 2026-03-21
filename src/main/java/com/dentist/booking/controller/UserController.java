package com.dentist.booking.controller;

import com.dentist.booking.dto.ApiResponse;
import com.dentist.booking.dto.AdminUserUpdateRequest;
import com.dentist.booking.dto.ProfileUpdateRequest;
import com.dentist.booking.entity.Role;
import com.dentist.booking.entity.User;
import com.dentist.booking.repository.UserRepository;
import com.dentist.booking.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "https://dentist-appointment-booking-platfor-one.vercel.app"})
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse> getMyProfile() {
        User user = userService.getCurrentUser();
        return ResponseEntity.ok(ApiResponse.success("Profile retrieved", user));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse> updateMyProfile(@Valid @RequestBody ProfileUpdateRequest request) {
        User user = userService.updateProfile(request);
        return ResponseEntity.ok(ApiResponse.success("User profile updated", user));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> getAllUsers(
            @RequestParam(required = false) String role) {
        List<User> users;
        if (role != null && !role.isEmpty()) {
            users = userRepository.findByRole(Role.valueOf(role.toUpperCase()));
        } else {
            users = userService.getAllUsers();
        }
        return ResponseEntity.ok(ApiResponse.success("Users retrieved", users));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success("User retrieved", user));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> updateUser(@PathVariable Long id, @RequestBody AdminUserUpdateRequest request) {
        User user = userService.updateUserByAdmin(id, request);
        return ResponseEntity.ok(ApiResponse.success("User updated successfully", user));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted"));
    }
}


