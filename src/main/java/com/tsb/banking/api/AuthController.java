package com.tsb.banking.api;

import com.tsb.banking.security.JwtService;
import com.tsb.banking.service.RefreshTokenService;
import com.tsb.banking.service.OtpService;
import com.tsb.banking.service.PasswordService;
import io.jsonwebtoken.JwtException;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author zhangyunlin
 *
 * Handles authentication: login, token refresh, password reset via OTP
 */

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final OtpService otpService;
    private final PasswordService passwordService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public AuthController(
            OtpService otpService,
            PasswordService passwordService,
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            RefreshTokenService refresh) {                     // <-- add to ctor
        this.otpService = otpService;
        this.passwordService = passwordService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.refreshTokenService = refresh;
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
    public record TokenPair(String accessToken, String refreshToken, String tokenType, long expiresInSeconds) {}
    public record RefreshRequest(@NotBlank String refreshToken) {}

    @Operation(summary = "Login with username/password and receive tokens")
    @PostMapping("/login")
    public ResponseEntity<TokenPair> login(@RequestBody @Valid LoginRequest req) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.username(), req.password()));
        UserDetails user = (UserDetails) auth.getPrincipal();

        // Issue access + refresh (and persist refresh with rotation)
        var pair = refreshTokenService.issueForUser(user.getUsername());
        return ResponseEntity.ok(new TokenPair(pair.accessToken(), pair.refreshToken(), "Bearer", pair.expiresInSeconds()));
    }

    @Operation(summary = "Refresh access token using refresh token (rotation)")
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody @Valid RefreshRequest req) {
        try {
            var pair = refreshTokenService.rotate(req.refreshToken());
            return ResponseEntity.ok(new TokenPair(pair.accessToken(), pair.refreshToken(), "Bearer", pair.expiresInSeconds()));
        } catch (IllegalArgumentException e) {
            // not a refresh token / malformed
            return ResponseEntity.badRequest().body(Map.of("status","fail","message", e.getMessage()));
        } catch (IllegalStateException | JwtException e) {
            // expired / revoked / unknown / invalid signature
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("status","fail","message","Invalid or expired refresh token"));
        }
    }
}
