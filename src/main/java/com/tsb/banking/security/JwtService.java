package com.tsb.banking.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * JWT service – issues/parses access & refresh tokens.
 */
@Service
public class JwtService {

    private final Key key;
    private final long accessTtlMinutes;
    private final long refreshTtlDays;
    private final String issuer;

    public JwtService(
            @Value("${app.security.jwt.secret}") String base64Secret,
            @Value("${app.security.jwt.access-ttl-minutes:15}") long accessTtlMinutes,
            @Value("${app.security.jwt.refresh-ttl-days:7}") long refreshTtlDays,
            @Value("${app.security.jwt.issuer:}") String issuer) {
        this.key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(base64Secret));
        this.accessTtlMinutes = accessTtlMinutes;
        this.refreshTtlDays = refreshTtlDays;
        this.issuer = issuer;
    }

    // Backward-compatible alias: generate an **access** token.
    public String generate(UserDetails user) {
        return generateAccess(user);
    }

    // Issue an access token.
    public String generateAccess(UserDetails user) {
        Instant now = Instant.now();
        List<String> roles = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).toList();

        JwtBuilder b = Jwts.builder()
                // jti
                .setId(UUID.randomUUID().toString())
                .setSubject(user.getUsername())
                .claim("roles", roles)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(accessTtlMinutes * 60)))
                .signWith(key, SignatureAlgorithm.HS256);
        if (!issuer.isBlank()){
            b.setIssuer(issuer);
        }
        return b.compact();
    }

    // Issue a refresh token.
    public String generateRefresh(UserDetails user) {
        Instant now = Instant.now();
        JwtBuilder b = Jwts.builder()
                // jti
                .setId(UUID.randomUUID().toString())
                .setSubject(user.getUsername())
                // mark token type
                .claim("type", "refresh")
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(refreshTtlDays * 24L * 3600L)))
                .signWith(key, SignatureAlgorithm.HS256);
        if (!issuer.isBlank()){
            b.setIssuer(issuer);
        }
        return b.compact();
    }

    public Jws<Claims> parse(String token) {
        JwtParserBuilder pb = Jwts.parserBuilder().setSigningKey(key);
        if (!issuer.isBlank()){
            pb.requireIssuer(issuer);
        }
        return pb.build().parseClaimsJws(token);
    }

    public String getUsername(String token) {
        return parse(token).getBody().getSubject();
    }

    public String getJti(String token)       {
        return parse(token).getBody().getId();
    }

    public Instant getExpiry(String token)   {
        return parse(token).getBody().getExpiration().toInstant();
    }

    public boolean isExpired(String token)   {
        return getExpiry(token).isBefore(Instant.now());
    }

    /** Return true if token has claim { "type": "refresh" } */
    public boolean isRefreshToken(String token) {
        Object v = parse(token).getBody().get("type");
        return v != null && "refresh".equals(v.toString());
    }

    public long getAccessTtlMinutes() {
        return accessTtlMinutes;
    }
    public long getRefreshTtlDays()   {
        return refreshTtlDays;
    }
}
