package mcm.app.service;

import mcm.app.dto.LoginRequest;
import mcm.app.dto.OtpRequest;
import mcm.app.dto.SignupRequest;
import mcm.app.entity.*;
import mcm.app.repository.*;
import mcm.app.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private OtpService otpService;

    // --- Login user and generate JWT ---
    public String login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            return jwtUtils.generateJwtToken(authentication.getName());
        } catch (AuthenticationException ex) {
            throw new RuntimeException("Invalid email or password");
        }
    }

    // --- Register new user (Step 1: Send OTP) ---
    public String sendOtp(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("User already exist");
        }
        return otpService.generateAndSendOtp(request.getEmail(), request.getFullName(), request.getPassword());
    }

    // --- Verify OTP and complete registration (Step 2) ---
    public String verifyOtpAndRegister(OtpRequest request) {
        OtpVerification otp = otpService.getPendingOtp(request.getEmail());
        if (otp == null) {
            throw new RuntimeException("No pending OTP verification found. Please register again.");
        }
        if (!otpService.verifyOtp(request.getEmail(), request.getOtpCode())) {
            throw new RuntimeException("Invalid or expired OTP");
        }

        User user = new User();
        user.setFullName(otp.getFullName());
        user.setEmail(otp.getEmail());
        user.setPassword(passwordEncoder.encode(otp.getPassword()));

        Set<Role> roles = new HashSet<>();
        Role customerRole = roleRepository.findByName("CUSTOMER")
                .orElseThrow(() -> new RuntimeException("Role not found: CUSTOMER"));
        roles.add(customerRole);
        user.setRoles(roles);

        userRepository.save(user);
        return "User registered successfully with CUSTOMER role!";
    }

    // --- Get currently authenticated user ---
    public User getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("No authenticated user found");
        }

        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }

    // --- Logout ---
    public void logout() {
        SecurityContextHolder.clearContext();
        // JWT token is stateless, actual cookie deletion handled in controller
    }
}