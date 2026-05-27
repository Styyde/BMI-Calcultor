package com.example.backend.bmicalculator.controller;

import com.example.backend.bmicalculator.dto.auth.AuthRequest;
import com.example.backend.bmicalculator.dto.auth.AuthResponse;
import com.example.backend.bmicalculator.dto.auth.RegisterRequest;
import com.example.backend.bmicalculator.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(
            @RequestHeader("Authorization") String authorization) {
        AuthResponse response = authService.refreshToken(authorization);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        // Le client supprime le token côté frontend
        // Optionnel: blacklist token avec Redis
        return ResponseEntity.ok().build();
    }
}