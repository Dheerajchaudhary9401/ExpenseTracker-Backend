package com.spendsmart.auth.service;

import com.spendsmart.auth.dto.*;
import com.spendsmart.auth.entity.User;

public interface AuthService {

    User register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    void logout(String token);

    boolean validateToken(String token);

    String refreshToken(String token);

    User getUserById(int userId);

    User getUserByEmail(String email);

    User updateProfile(int userId, ProfileUpdateRequest request);

    void changePassword(int userId, PasswordChangeRequest request);

    void updateCurrency(int userId, String currency);

    void updateMonthlyBudget(int userId, double budget);

    void deactivateAccount(int userId);
}