package com.newwork.employeeprofile.controller;

import com.newwork.employeeprofile.dto.request.CreateEmployeeRequest;
import com.newwork.employeeprofile.dto.request.LoginRequest;
import com.newwork.employeeprofile.dto.response.AuthResponse;
import com.newwork.employeeprofile.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication and registration endpoints")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new employee", description = "Creates a new employee account and returns JWT token")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody CreateEmployeeRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticates an employee and returns JWT token")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        //return ResponseEntity.ok(authService.login(request));
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }
}
