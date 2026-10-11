package com.feetrack.controller;

import com.feetrack.dto.AuthDto;
import com.feetrack.model.User;
import com.feetrack.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/student/register")
    public ResponseEntity<AuthDto.AuthResponse> registerStudent(@Valid @RequestBody AuthDto.StudentRegisterRequest request) {
        AuthDto.AuthResponse response = authService.registerStudent(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/student/login")
    public ResponseEntity<AuthDto.AuthResponse> studentLogin(@Valid @RequestBody AuthDto.LoginRequest request) {
        AuthDto.AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthDto.AuthResponse> login(@Valid @RequestBody AuthDto.LoginRequest request) {
        AuthDto.AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout() {
        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("message", "Logged out successfully");
        return ResponseEntity.ok(res);
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser() {
        User user = authService.getAuthenticatedUser();
        Map<String, Object> res = new HashMap<>();
        res.put("id", user.getId());
        res.put("name", user.getName());
        res.put("email", user.getEmail());
        res.put("role", user.getRole().name());
        res.put("phone", user.getPhone());
        res.put("status", user.getStatus());
        res.put("studentProfileId", user.getStudentProfileId());
        return ResponseEntity.ok(res);
    }
}
