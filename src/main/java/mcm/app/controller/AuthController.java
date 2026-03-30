package mcm.app.controller;

import mcm.app.dto.*;
import mcm.app.entity.Role;
import mcm.app.entity.User;
import mcm.app.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    // --- Login ---
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            String token = authService.login(request);

            ResponseCookie cookie = ResponseCookie.from("JWT_TOKEN", token)
                    .httpOnly(true)
                    .secure(false)      // set true in production
                    .path("/")
                    .maxAge(86400)      // 1 day
                    .sameSite("Strict")
                    .build();

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(Map.of("message", "Login successful"));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", ex.getMessage()));
        }
    }

    // --- Registration Step 1: Send OTP ---
    @PostMapping("/register/send-otp")
    public ResponseEntity<?> sendOtp(@RequestBody SignupRequest request) {
        try {
            String message = authService.sendOtp(request);
            return ResponseEntity.ok(Map.of("message", message));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", ex.getMessage()));
        }
    }

    // --- Registration Step 2: Verify OTP and Complete Registration ---
    @PostMapping("/register/verify")
    public ResponseEntity<?> verifyOtp(@RequestBody OtpRequest request) {
        try {
            String message = authService.verifyOtpAndRegister(request);
            return ResponseEntity.ok(Map.of("message", message));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", ex.getMessage()));
        }
    }

    // --- Logout ---
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        ResponseCookie cookie = ResponseCookie.from("JWT_TOKEN", "")
                .httpOnly(true)
                .secure(false)      // true in production
                .path("/")
                .maxAge(0)          // delete cookie
                .sameSite("Strict")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(Map.of("message", "Logged out successfully"));
    }

    // --- Get current authenticated user ---

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Not authenticated");
        }

        User user = authService.getCurrentUser(authentication);

        Set<String> roles = user.getRoles()
                .stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        AuthMeResponse response = new AuthMeResponse(
                user.getFullName(),
                user.getEmail(),
                roles
        );

        return ResponseEntity.ok(response);
    }

    // --- Global Exception Handler ---
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleExceptions(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", ex.getMessage()));
    }
}