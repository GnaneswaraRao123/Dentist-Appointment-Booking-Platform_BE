package com.dentist.booking.controller;

import com.dentist.booking.dto.*;
import com.dentist.booking.security.JwtService;
import com.dentist.booking.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "https://dentist-appointment-booking-platfor-one.vercel.app"})
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(ApiResponse.success("Registration successful", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse> getCurrentUser(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(ApiResponse.error("No token provided"));
        }
        try {
            String token = authHeader.substring(7);
            String email = jwtService.extractUsername(token);
            AuthResponse response = authService.getCurrentUser(email);
            return ResponseEntity.ok(ApiResponse.success("User retrieved", response));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(ApiResponse.error("Invalid token"));
        }
    }
}


