package com.freightfox.assignment.auth.controller;

import com.freightfox.assignment.auth.dto.AdminProfileResponse;
import com.freightfox.assignment.auth.dto.LoginRequest;
import com.freightfox.assignment.auth.dto.LoginResponse;
import com.freightfox.assignment.auth.dto.RegisterRequest;
import com.freightfox.assignment.auth.service.AuthService;
import org.springframework.security.core.Authentication;
import com.freightfox.assignment.auth.dto.AdminProfileResponse;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(
            @Valid @RequestBody RegisterRequest request,
            @RequestHeader("X-Admin-Bootstrap-Secret") String bootstrapSecret) {

        authService.registerAdmin(request, bootstrapSecret);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    public ResponseEntity<AdminProfileResponse> getCurrentAdmin(
            Authentication authentication) {

        return ResponseEntity.ok(
                authService.getCurrentAdmin(authentication.getName())
        );
    }
}