package com.tsb.banking.api;

import com.tsb.banking.service.OtpService;
import com.tsb.banking.service.PasswordService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthResetController {

    private final OtpService otpService;
    private final PasswordService passwordService;

    public AuthResetController(OtpService otpService, PasswordService passwordService) {
        this.otpService = otpService;
        this.passwordService = passwordService;
    }

    public record RequestResetDto(@NotBlank String identifier) {}
    public record RequestResetResponse(String sentToMasked) {}

    @Operation(summary = "Request password reset OTP (SMS)")
    @PostMapping("/request-reset")
    public ResponseEntity<RequestResetResponse> requestReset(@RequestBody RequestResetDto req) {
        String masked = otpService.requestPasswordReset(req.identifier());
        return ResponseEntity.ok(new RequestResetResponse(masked));
    }

    public record ConfirmResetDto(@NotBlank String identifier, @NotBlank String code,
                                  @NotBlank String newPassword) {}

    @Operation(summary = "Confirm password reset with OTP")
    @PostMapping("/confirm-reset")
    public ResponseEntity<String> confirmReset(@RequestBody ConfirmResetDto req) {
        otpService.confirmPasswordReset(req.identifier(), req.code(), req.newPassword(), passwordService);
        return ResponseEntity.ok("Password reset successful");
    }
}
