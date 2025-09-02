package com.tsb.banking.service;

import com.tsb.banking.auth.sms.SmsSender;
import com.tsb.banking.exception.BusinessException;
import com.tsb.banking.model.OtpToken;
import com.tsb.banking.model.User;
import com.tsb.banking.repo.OtpTokenRepository;
import com.tsb.banking.repo.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;

@Service
public class OtpService {

    private final OtpTokenRepository otpRepo;
    private final UserRepository userRepo;
    private final SmsSender smsSender;
    private final long ttlMinutes;
    private final int maxRequestsPerHour;

    public OtpService(OtpTokenRepository otpRepo,
                      UserRepository userRepo,
                      SmsSender smsSender,
                      @Value("${app.otp.ttl-minutes:10}") long ttlMinutes,
                      @Value("${app.otp.max-req-per-hour:5}") int maxRequestsPerHour) {
        this.otpRepo = otpRepo;
        this.userRepo = userRepo;
        this.smsSender = smsSender;
        this.ttlMinutes = ttlMinutes;
        this.maxRequestsPerHour = maxRequestsPerHour;
    }

    private static String sha256(String s) {
        try {
            var md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(s.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String generateCode() {
        SecureRandom r = new SecureRandom();
        int code = 100000 + r.nextInt(900000); // 6 digits
        return Integer.toString(code);
    }

    @Transactional
    public String requestPasswordReset(String identifier) {
        User user = userRepo.findByIdentifier(identifier)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "User not found"));

        String phone = user.getPhone();
        if (phone == null || phone.isBlank()) {
            throw new BusinessException("NO_PHONE", "User has no phone number on file");
        }

        // Simple rate limit per destination per hour
        long recent = otpRepo.countByDestinationAndPurposeAndCreatedAtAfter(
                phone, "PASSWORD_RESET", Instant.now().minus(1, ChronoUnit.HOURS));
        if (recent >= maxRequestsPerHour) {
            throw new BusinessException("RATE_LIMIT", "Too many OTP requests, please try later");
        }

        // Issue OTP
        String code = generateCode();
        OtpToken token = new OtpToken();
        token.setUser(user);
        token.setDestination(phone);
        token.setPurpose("PASSWORD_RESET");
        token.setCodeHash(sha256(code));
        token.setExpiresAt(Instant.now().plus(ttlMinutes, ChronoUnit.MINUTES));
        otpRepo.save(token);

        // Send SMS (mock)
        smsSender.send(phone, "Your reset code is: " + code + " (valid " + ttlMinutes + " minutes)");

        // Return masked phone for UX
        return mask(phone);
    }

    @Transactional
    public void confirmPasswordReset(String identifier, String code, String newPassword, PasswordService passwordService) {
        User user = userRepo.findByIdentifier(identifier)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "User not found"));

        OtpToken token = otpRepo.findTopByUserAndPurposeAndConsumedFalseOrderByCreatedAtDesc(user, "PASSWORD_RESET")
                .orElseThrow(() -> new BusinessException("OTP_NOT_FOUND", "No active OTP token"));

        if (token.getExpiresAt().isBefore(Instant.now())) {
            token.setConsumed(true);
            otpRepo.save(token);
            throw new BusinessException("OTP_EXPIRED", "OTP has expired");
        }

        if (!sha256(code).equalsIgnoreCase(token.getCodeHash())) {
            int attempts = token.getAttempts() + 1;
            token.setAttempts(attempts);
            otpRepo.save(token);
            if (attempts >= 5) {
                token.setConsumed(true);
                otpRepo.save(token);
                throw new BusinessException("OTP_LOCKED", "Too many attempts, token locked");
            }
            throw new BusinessException("OTP_INVALID", "Invalid code");
        }

        // Success: consume token and reset password
        token.setConsumed(true);
        otpRepo.save(token);

        passwordService.updatePassword(user, newPassword);
    }

    private String mask(String phone) {
        if (phone == null || phone.length() < 4){
            return "****";
        }
        return phone.substring(0, 2) + "****" + phone.substring(phone.length() - 2);
    }
}
