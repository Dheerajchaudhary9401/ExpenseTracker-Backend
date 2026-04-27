package com.spendsmart.auth.resource;

import com.spendsmart.auth.dto.*;
import com.spendsmart.auth.entity.User;
import com.spendsmart.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthResource {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<User> register(
            @Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            @RequestHeader("Authorization") String token) {
        authService.logout(token.replace("Bearer ", ""));
        return ResponseEntity.ok("Logged out successfully");
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refresh(
            @RequestHeader("Authorization") String token) {
        String newToken = authService.refreshToken(
                token.replace("Bearer ", ""));
        return ResponseEntity.ok(Map.of("token", newToken));
    }

    @GetMapping("/profile/{userId}")
    public ResponseEntity<User> getProfile(
            @PathVariable int userId) {
        return ResponseEntity.ok(authService.getUserById(userId));
    }

    @PutMapping("/profile/{userId}")
    public ResponseEntity<User> updateProfile(
            @PathVariable int userId,
            @RequestBody ProfileUpdateRequest request) {
        return ResponseEntity.ok(authService.updateProfile(userId, request));
    }

    @PutMapping("/password/{userId}")
    public ResponseEntity<String> changePassword(
            @PathVariable int userId,
            @Valid @RequestBody PasswordChangeRequest request) {
        authService.changePassword(userId, request);
        return ResponseEntity.ok("Password updated successfully");
    }

    @PutMapping("/currency/{userId}")
    public ResponseEntity<String> updateCurrency(
            @PathVariable int userId,
            @RequestBody Map<String, String> body) {
        authService.updateCurrency(userId, body.get("currency"));
        return ResponseEntity.ok("Currency updated");
    }

    @PutMapping("/budget/{userId}")
    public ResponseEntity<String> updateBudget(
            @PathVariable int userId,
            @RequestBody Map<String, Double> body) {
        authService.updateMonthlyBudget(userId, body.get("monthlyBudget"));
        return ResponseEntity.ok("Monthly budget updated");
    }

    @DeleteMapping("/deactivate/{userId}")
    public ResponseEntity<String> deactivate(
            @PathVariable int userId) {
        authService.deactivateAccount(userId);
        return ResponseEntity.ok("Account deactivated");
    }

    @GetMapping("/validate")
    public ResponseEntity<Boolean> validate(
            @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(
                authService.validateToken(token.replace("Bearer ", "")));
    }
}