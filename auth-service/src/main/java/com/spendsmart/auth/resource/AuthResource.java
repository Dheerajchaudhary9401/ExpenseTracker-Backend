package com.spendsmart.auth.resource;

import com.spendsmart.auth.dto.GoogleAuthRequest;
import com.spendsmart.auth.service.impl.GoogleAuthService;
import com.spendsmart.auth.dto.*;
import com.spendsmart.auth.entity.User;
import com.spendsmart.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.spendsmart.auth.repository.UserRepository;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthResource {

    private final UserRepository userRepository;
    private final AuthService authService;
    private final GoogleAuthService googleAuthService;

    @PostMapping("/google")
    public ResponseEntity<LoginResponse> googleLogin(@RequestBody GoogleAuthRequest request) {
        LoginResponse response = googleAuthService.loginWithGoogle(request.getIdToken());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(
            @Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }


    // GET /auth/users — list all users (admin only)
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    // PUT /auth/promote/{userId} — promote to ADMIN
    @PutMapping("/promote/{userId}")
    public ResponseEntity<String> promoteToAdmin(@PathVariable int userId) {
        User user = authService.getUserById(userId);
        user.setRole("ADMIN");
        userRepository.save(user);
        return ResponseEntity.ok("User promoted to ADMIN");
    }

    // PUT /auth/reactivate/{userId} — reactivate account
    @PutMapping("/reactivate/{userId}")
    public ResponseEntity<String> reactivate(@PathVariable int userId) {
        User user = authService.getUserById(userId);
        user.setActive(true);
        userRepository.save(user);
        return ResponseEntity.ok("Account reactivated");
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

    // DELETE /auth/users/{userId} — permanent hard delete (admin only)
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable int userId) {
        authService.deleteUser(userId);
        return ResponseEntity.ok("User permanently deleted");
    }

    @GetMapping("/validate")
    public ResponseEntity<Boolean> validate(
            @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(
                authService.validateToken(token.replace("Bearer ", "")));
    }
}