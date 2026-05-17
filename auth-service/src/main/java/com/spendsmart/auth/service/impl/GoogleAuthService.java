package com.spendsmart.auth.service.impl;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.spendsmart.auth.dto.LoginResponse;
import com.spendsmart.auth.entity.User;
import com.spendsmart.auth.repository.UserRepository;
import com.spendsmart.auth.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.util.Collections;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleAuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    // Your Google Client ID
    @Value("${google.client-id}")
    private String clientId;

    /**
     * Verifies the Google ID token, finds or creates the user, and returns a JWT.
     */
    public LoginResponse loginWithGoogle(String idToken) {
        try {
            // Step 1: Verify the Google token
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(),
                    GsonFactory.getDefaultInstance()
            )
                    .setAudience(Collections.singletonList(clientId))
                    .build();

            GoogleIdToken googleIdToken = verifier.verify(idToken);
            if (googleIdToken == null) {
                throw new RuntimeException("Invalid Google token");
            }

            // Step 2: Extract user info from Google token
            GoogleIdToken.Payload payload = googleIdToken.getPayload();
            String email = payload.getEmail();
            //String fullNameRaw = (String) payload.get("name");
            String pictureUrl = (String) payload.get("picture");

//            if (fullName == null || fullName.isBlank()) {
//                fullName = email.split("@")[0]; // fallback to email prefix
//            }
            String fullNameRaw = (String) payload.get("name");
            final String fullName = (fullNameRaw == null || fullNameRaw.isBlank())
                    ? email.split("@")[0]
                    : fullNameRaw;

            // Step 3: Find existing user or create new one
            User user = userRepository.findByEmail(email).orElseGet(() -> {
                User newUser = new User();
                newUser.setEmail(email);
                newUser.setFullName(fullName);
                newUser.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
                newUser.setCurrency("INR");
                newUser.setTimezone("Asia/Kolkata");
                newUser.setProvider("GOOGLE");
                newUser.setActive(true);
                newUser.setRole("USER");  //new
                if (pictureUrl != null) {
                    newUser.setAvatarUrl(pictureUrl);
                }
                log.info("Creating new user from Google login: {}", email);
                return userRepository.save(newUser);
            });

            // Step 4: Generate JWT token
            String token = jwtUtil.generateToken(user.getEmail(), user.getUserId());

            // Step 5: Return LoginResponse
            return new LoginResponse(
                    token,
                    "Bearer",
                    user.getUserId(),
                    user.getFullName(),
                    user.getEmail(),
                    user.getCurrency(),
                    user.getRole()
            );

        } catch (Exception e) {
            log.error("Google authentication failed: {}", e.getMessage());
            throw new RuntimeException("Google authentication failed: " + e.getMessage());
        }
    }
}