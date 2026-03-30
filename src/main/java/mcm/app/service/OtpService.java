package mcm.app.service;

import mcm.app.entity.OtpVerification;
import mcm.app.repository.OtpVerificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class OtpService {

    @Autowired
    private OtpVerificationRepository otpRepository;

    @Autowired
    private JavaMailSender mailSender;

    private static final int OTP_EXPIRY_MINUTES = 10;

    @Transactional
    public String generateAndSendOtp(String email, String fullName, String password) {
        otpRepository.deleteByEmail(email);

        String otpCode = String.format("%06d", new Random().nextInt(999999));

        OtpVerification otp = new OtpVerification();
        otp.setEmail(email);
        otp.setOtpCode(otpCode);
        otp.setFullName(fullName);
        otp.setPassword(password);
        otp.setExpiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES));
        otpRepository.save(otp);

        sendOtpEmail(email, otpCode);

        return "OTP sent to your email";
    }

    @Transactional
    public boolean verifyOtp(String email, String otpCode) {
        return otpRepository.findTopByEmailAndVerifiedFalseOrderByExpiresAtDesc(email)
                .filter(otp -> !otp.isExpired() && otp.getOtpCode().equals(otpCode))
                .map(otp -> {
                    otp.setVerified(true);
                    otpRepository.save(otp);
                    return true;
                })
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public OtpVerification getPendingOtp(String email) {
        return otpRepository.findTopByEmailAndVerifiedFalseOrderByExpiresAtDesc(email)
                .filter(otp -> !otp.isExpired())
                .orElse(null);
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void cleanupExpiredOtps() {
        otpRepository.deleteAll(
            otpRepository.findAll().stream()
                .filter(OtpVerification::isExpired)
                .toList()
        );
    }

    private void sendOtpEmail(String to, String otpCode) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Your Verification Code");
            message.setText("Your verification code is: " + otpCode + "\n\nThis code expires in 10 minutes.");
            mailSender.send(message);
        } catch (Exception e) {
            System.out.println("Failed to send email: " + e.getMessage());
        }
    }
}
