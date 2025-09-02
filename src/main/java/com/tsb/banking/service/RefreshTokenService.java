package com.tsb.banking.service;

import com.tsb.banking.model.RefreshToken;
import com.tsb.banking.model.User;
import com.tsb.banking.repo.RefreshTokenRepository;
import com.tsb.banking.repo.UserRepository;
import com.tsb.banking.security.JwtService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class RefreshTokenService {
    private final RefreshTokenRepository repo;
    private final JwtService jwt;
    private final UserRepository users;

    public RefreshTokenService(RefreshTokenRepository repo, JwtService jwt, UserRepository users) {
        this.repo = repo; this.jwt = jwt; this.users = users;
    }

    public record Pair(String accessToken, String refreshToken, long expiresInSeconds) {}

    /** On login: issue access + refresh and persist the refresh by JTI */
    @Transactional
    public Pair issueForUser(String username) {
        User u = users.findByUsernameIgnoreCase(username).orElseThrow();
        var userDetails = org.springframework.security.core.userdetails.User
                .withUsername(u.getUsername())
                .password(u.getPasswordHash())
                .authorities(u.getRoles().split(","))
                .build();

        String refresh = jwt.generateRefresh(userDetails);
        RefreshToken row = new RefreshToken();
        row.setUser(u);
        row.setJti(jwt.getJti(refresh));
        row.setExpiresAt(jwt.getExpiry(refresh));
        repo.save(row);

        String access = jwt.generateAccess(userDetails);
        return new Pair(access, refresh, jwt.getAccessTtlMinutes() * 60);
    }

    /** On refresh: rotate refresh token (current -> revoked, new pair issued) */
    @Transactional
    public Pair rotate(String refreshToken) {
        if (!jwt.isRefreshToken(refreshToken)) {
            throw new IllegalArgumentException("Not a refresh token");
        }
        String jti = jwt.getJti(refreshToken);
        RefreshToken current = repo.findByJti(jti).orElseThrow(() ->
                new IllegalStateException("Refresh token not recognized"));
        if (current.isRevoked() || current.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalStateException("Refresh token expired or revoked");
        }
        current.setRevoked(true);
        repo.save(current);

        String username = jwt.getUsername(refreshToken);
        // creates the replacement
        Pair pair = issueForUser(username);
        current.setReplacedByJti(jwt.getJti(pair.refreshToken()));
        repo.save(current);

        // cleanup
        repo.deleteByExpiresAtBefore(Instant.now());
        return pair;
    }
}