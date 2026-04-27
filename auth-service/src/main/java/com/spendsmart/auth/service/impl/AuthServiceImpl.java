package com.spendsmart.auth.service.impl;

import com.spendsmart.auth.dto.*;
import com.spendsmart.auth.entity.User;
import com.spendsmart.auth.repository.UserRepository;
import com.spendsmart.auth.service.AuthService;
import com.spendsmart.auth.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public User register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }
        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .currency(request.getCurrency())
                .timezone(request.getTimezone())
                .provider("LOCAL")
                .isActive(true)
                .build();
        return userRepository.save(user);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.isActive()) {
            throw new RuntimeException("Account is deactivated");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials");
        }
        String token = jwtUtil.generateToken(user.getEmail(), user.getUserId());
        return new LoginResponse(
                token, "Bearer",
                user.getUserId(),
                user.getFullName(),
                user.getEmail(),
                user.getCurrency()
        );
    }

    @Override
    public void logout(String token) {
        // Client side removes token
        // Redis blacklist can be added later
    }

    @Override
    public boolean validateToken(String token) {
        return jwtUtil.validateToken(token);
    }

    @Override
    public String refreshToken(String token) {
        if (!jwtUtil.validateToken(token)) {
            throw new RuntimeException("Invalid or expired token");
        }
        String email = jwtUtil.extractEmail(token);
        int userId = jwtUtil.extractUserId(token);
        return jwtUtil.generateToken(email, userId);
    }

    @Override
    public User getUserById(int userId) {
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public User updateProfile(int userId, ProfileUpdateRequest request) {
        User user = getUserById(userId);
        if (request.getFullName() != null) user.setFullName(request.getFullName());
        if (request.getBio() != null)       user.setBio(request.getBio());
        if (request.getAvatarUrl() != null) user.setAvatarUrl(request.getAvatarUrl());
        if (request.getTimezone() != null)  user.setTimezone(request.getTimezone());
        return userRepository.save(user);
    }

    @Override
    public void changePassword(int userId, PasswordChangeRequest request) {
        User user = getUserById(userId);
        if (!passwordEncoder.matches(
                request.getCurrentPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Current password is incorrect");
        }
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    public void updateCurrency(int userId, String currency) {
        User user = getUserById(userId);
        user.setCurrency(currency);
        userRepository.save(user);
    }

    @Override
    public void updateMonthlyBudget(int userId, double budget) {
        User user = getUserById(userId);
        user.setMonthlyBudget(budget);
        userRepository.save(user);
    }

    @Override
    public void deactivateAccount(int userId) {
        User user = getUserById(userId);
        user.setActive(false);
        userRepository.save(user);
    }
}