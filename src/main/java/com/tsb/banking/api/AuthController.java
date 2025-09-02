package com.tsb.banking.api;

import com.tsb.banking.security.JwtService;
import com.tsb.banking.service.OtpService;
import com.tsb.banking.service.PasswordService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final OtpService otpService;
    private final PasswordService passwordService;

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthController(OtpService otpService, PasswordService passwordService,AuthenticationManager authenticationManager, JwtService jwtService) {
        this.otpService = otpService;
        this.passwordService = passwordService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
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


    public record LoginRequest(@NotBlank String username, @NotBlank String password) {}
    public record TokenResponse(String accessToken, String tokenType, long expiresInSeconds) {}

    @Operation(summary = "Login with username/password and receive a JWT access token")
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest req) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.username(), req.password()));
        UserDetails user = (UserDetails) auth.getPrincipal();
        String token = jwtService.generate(user);
        long ttl = jwtService.getAccessTtlMinutes() * 60;
        return ResponseEntity.ok(new TokenResponse(token, "Bearer", ttl));
    }
}
