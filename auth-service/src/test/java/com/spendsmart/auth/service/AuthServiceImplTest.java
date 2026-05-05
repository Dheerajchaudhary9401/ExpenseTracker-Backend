package com.spendsmart.auth.service;

import com.spendsmart.auth.dto.*;
import com.spendsmart.auth.entity.User;
import com.spendsmart.auth.repository.UserRepository;
import com.spendsmart.auth.service.impl.AuthServiceImpl;
import com.spendsmart.auth.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl - Unit Tests")
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthServiceImpl authService;

    // Shared test data — recreated fresh before each test
    private User sampleUser;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {

        sampleUser = User.builder()
                .userId(1)
                .fullName("Rahul Sharma")
                .email("rahul@example.com")
                .passwordHash("$2a$10$hashedPassword")
                .currency("INR")
                .timezone("Asia/Kolkata")
                .provider("LOCAL")
                .isActive(true)
                .build();

        registerRequest = new RegisterRequest();
        registerRequest.setFullName("Rahul Sharma");
        registerRequest.setEmail("rahul@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setCurrency("INR");
        registerRequest.setTimezone("Asia/Kolkata");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("rahul@example.com");
        loginRequest.setPassword("password123");
    }

    @Nested
    @DisplayName("register() Tests")
    class RegisterTests {

        @Test
        @DisplayName("Should successfully register a new user")
        void register_NewUser_Success() {

            when(userRepository.existsByEmail("rahul@example.com")).thenReturn(false);

            when(passwordEncoder.encode("password123")).thenReturn("$2a$10$hashedPassword");

            when(userRepository.save(any(User.class))).thenReturn(sampleUser);

            User result = authService.register(registerRequest);

            assertNotNull(result);                                    // result should not be null
            assertEquals("Rahul Sharma", result.getFullName());      // name must match
            assertEquals("rahul@example.com", result.getEmail());    // email must match
            assertTrue(result.isActive());                           // user should be active

            verify(userRepository, times(1)).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw exception when email is already registered")
        void register_DuplicateEmail_ThrowsException() {
            when(userRepository.existsByEmail("rahul@example.com")).thenReturn(true);

            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> authService.register(registerRequest));

            assertEquals("Email already registered", exception.getMessage());

            verify(userRepository, never()).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("login() Tests")
    class LoginTests {

        @Test
        @DisplayName("Should return LoginResponse with token on valid credentials")
        void login_ValidCredentials_ReturnsToken() {
            // ARRANGE
            // Step 1: findByEmail returns our sample user
            when(userRepository.findByEmail("rahul@example.com"))
                    .thenReturn(Optional.of(sampleUser));

            // Step 2: password matches
            when(passwordEncoder.matches("password123", "$2a$10$hashedPassword"))
                    .thenReturn(true);

            // Step 3: JWT is generated
            when(jwtUtil.generateToken("rahul@example.com", 1))
                    .thenReturn("mock.jwt.token");

            // ACT
            LoginResponse response = authService.login(loginRequest);

            // ASSERT — check every field of the response
            assertNotNull(response);
            assertEquals("mock.jwt.token", response.getToken());
            assertEquals("Bearer", response.getTokenType());
            assertEquals(1, response.getUserId());
            assertEquals("Rahul Sharma", response.getFullName());
            assertEquals("rahul@example.com", response.getEmail());
            assertEquals("INR", response.getCurrency());
        }

        @Test
        @DisplayName("Should throw exception when email does not exist")
        void login_EmailNotFound_ThrowsException() {
            // ARRANGE — simulate no user with this email in DB
            when(userRepository.findByEmail("rahul@example.com"))
                    .thenReturn(Optional.empty()); // Optional.empty() = nothing found

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> authService.login(loginRequest));

            assertEquals("User not found", ex.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when account is deactivated")
        void login_AccountDeactivated_ThrowsException() {
            // ARRANGE — user exists but isActive = false
            sampleUser.setActive(false);
            when(userRepository.findByEmail("rahul@example.com"))
                    .thenReturn(Optional.of(sampleUser));

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> authService.login(loginRequest));

            assertEquals("Account is deactivated", ex.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when password is incorrect")
        void login_WrongPassword_ThrowsException() {
            // ARRANGE — user found, but password does not match
            when(userRepository.findByEmail("rahul@example.com"))
                    .thenReturn(Optional.of(sampleUser));

            // matches() returns false → wrong password
            when(passwordEncoder.matches("password123", "$2a$10$hashedPassword"))
                    .thenReturn(false);

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> authService.login(loginRequest));

            assertEquals("Invalid credentials", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("Token Tests (validateToken & refreshToken)")
    class TokenTests {

        @Test
        @DisplayName("validateToken() - should return true for a valid JWT")
        void validateToken_ValidToken_ReturnsTrue() {
            when(jwtUtil.validateToken("valid.jwt.token")).thenReturn(true);

            boolean result = authService.validateToken("valid.jwt.token");

            assertTrue(result); // must be true
        }

        @Test
        @DisplayName("validateToken() - should return false for an invalid or expired JWT")
        void validateToken_InvalidToken_ReturnsFalse() {
            when(jwtUtil.validateToken("expired.token")).thenReturn(false);

            boolean result = authService.validateToken("expired.token");

            assertFalse(result); // must be false
        }

        @Test
        @DisplayName("refreshToken() - should return a new token for a still-valid token")
        void refreshToken_ValidToken_ReturnsNewToken() {
            // ARRANGE — old token is still valid
            when(jwtUtil.validateToken("old.token")).thenReturn(true);
            when(jwtUtil.extractEmail("old.token")).thenReturn("rahul@example.com");
            when(jwtUtil.extractUserId("old.token")).thenReturn(1);
            when(jwtUtil.generateToken("rahul@example.com", 1)).thenReturn("new.token");

            // ACT
            String result = authService.refreshToken("old.token");

            // ASSERT
            assertEquals("new.token", result);
        }

        @Test
        @DisplayName("refreshToken() - should throw exception for an invalid/expired token")
        void refreshToken_InvalidToken_ThrowsException() {
            when(jwtUtil.validateToken("bad.token")).thenReturn(false);

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> authService.refreshToken("bad.token"));

            assertEquals("Invalid or expired token", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("User Lookup Tests")
    class UserLookupTests {

        @Test
        @DisplayName("getUserById() - should return user when found")
        void getUserById_Found_ReturnsUser() {
            when(userRepository.findByUserId(1)).thenReturn(Optional.of(sampleUser));

            User result = authService.getUserById(1);

            assertEquals(1, result.getUserId());
            assertEquals("Rahul Sharma", result.getFullName());
        }

        @Test
        @DisplayName("getUserById() - should throw exception when user not found")
        void getUserById_NotFound_ThrowsException() {
            when(userRepository.findByUserId(99)).thenReturn(Optional.empty());

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> authService.getUserById(99));

            assertEquals("User not found", ex.getMessage());
        }

        @Test
        @DisplayName("getUserByEmail() - should return user when found")
        void getUserByEmail_Found_ReturnsUser() {
            when(userRepository.findByEmail("rahul@example.com"))
                    .thenReturn(Optional.of(sampleUser));

            User result = authService.getUserByEmail("rahul@example.com");

            assertEquals("rahul@example.com", result.getEmail());
        }

        @Test
        @DisplayName("getUserByEmail() - should throw exception when not found")
        void getUserByEmail_NotFound_ThrowsException() {
            when(userRepository.findByEmail("ghost@example.com"))
                    .thenReturn(Optional.empty());

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> authService.getUserByEmail("ghost@example.com"));

            assertEquals("User not found", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("Profile & Account Management Tests")
    class ProfileTests {

        @Test
        @DisplayName("updateProfile() - should update only non-null fields and save")
        void updateProfile_UpdatesFields_AndSaves() {
            // ARRANGE — only fullName and bio are being updated
            ProfileUpdateRequest req = new ProfileUpdateRequest();
            req.setFullName("Rahul Updated");
            req.setBio("I love tracking expenses!");

            when(userRepository.findByUserId(1)).thenReturn(Optional.of(sampleUser));
            when(userRepository.save(any(User.class))).thenReturn(sampleUser);

            // ACT
            authService.updateProfile(1, req);

            assertEquals("Rahul Updated", sampleUser.getFullName());
            assertEquals("I love tracking expenses!", sampleUser.getBio());

            // Verify it was saved to the repository.
            verify(userRepository, times(1)).save(sampleUser);
        }

        @Test
        @DisplayName("changePassword() - should hash new password and save on success")
        void changePassword_CorrectCurrentPassword_SavesNewHash() {
            PasswordChangeRequest req = new PasswordChangeRequest();
            req.setCurrentPassword("password123");
            req.setNewPassword("newSecurePass1!");

            when(userRepository.findByUserId(1)).thenReturn(Optional.of(sampleUser));

            // Current password matches.
            when(passwordEncoder.matches("password123", "$2a$10$hashedPassword"))
                    .thenReturn(true);

            // Encoding the new password returns a new hash.
            when(passwordEncoder.encode("newSecurePass1!")).thenReturn("$2a$10$newHash");

            when(userRepository.save(any(User.class))).thenReturn(sampleUser);

            assertDoesNotThrow(() -> authService.changePassword(1, req));

            // The user's password hash must be updated in memory before saving.
            assertEquals("$2a$10$newHash", sampleUser.getPasswordHash());

            verify(userRepository, times(1)).save(sampleUser);
        }

        @Test
        @DisplayName("changePassword() - should throw exception when current password is wrong")
        void changePassword_WrongCurrentPassword_ThrowsException() {
            PasswordChangeRequest req = new PasswordChangeRequest();
            req.setCurrentPassword("wrongPassword");
            req.setNewPassword("newSecurePass1!");

            when(userRepository.findByUserId(1)).thenReturn(Optional.of(sampleUser));

            // Password does NOT match.
            when(passwordEncoder.matches("wrongPassword", "$2a$10$hashedPassword"))
                    .thenReturn(false);

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> authService.changePassword(1, req));

            assertEquals("Current password is incorrect", ex.getMessage());

            // save() should never be called if validation failed.
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("updateCurrency() - should update currency and save")
        void updateCurrency_UpdatesAndSaves() {
            when(userRepository.findByUserId(1)).thenReturn(Optional.of(sampleUser));
            when(userRepository.save(any(User.class))).thenReturn(sampleUser);

            authService.updateCurrency(1, "USD");

            assertEquals("USD", sampleUser.getCurrency());
            verify(userRepository, times(1)).save(sampleUser);
        }

        @Test
        @DisplayName("updateMonthlyBudget() - should update budget and save")
        void updateMonthlyBudget_UpdatesAndSaves() {
            when(userRepository.findByUserId(1)).thenReturn(Optional.of(sampleUser));
            when(userRepository.save(any(User.class))).thenReturn(sampleUser);

            authService.updateMonthlyBudget(1, 50000.0);

            assertEquals(50000.0, sampleUser.getMonthlyBudget());
            verify(userRepository, times(1)).save(sampleUser);
        }

        @Test
        @DisplayName("deactivateAccount() - should set isActive to false and save")
        void deactivateAccount_SetsInactiveAndSaves() {
            when(userRepository.findByUserId(1)).thenReturn(Optional.of(sampleUser));
            when(userRepository.save(any(User.class))).thenReturn(sampleUser);

            authService.deactivateAccount(1);

            // After deactivation the user must be inactive.
            assertFalse(sampleUser.isActive());
            verify(userRepository, times(1)).save(sampleUser);
        }
    }
}