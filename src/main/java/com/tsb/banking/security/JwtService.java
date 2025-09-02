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

@Service
public class JwtService {

    private final Key key;
    private final long accessTtlMinutes;
    private final String issuer;

    public JwtService(
            @Value("${app.security.jwt.secret}") String base64Secret,
            @Value("${app.security.jwt.access-ttl-minutes:15}") long accessTtlMinutes,
            @Value("${app.security.jwt.issuer:}") String issuer) {
        this.key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(base64Secret));
        this.accessTtlMinutes = accessTtlMinutes;
        this.issuer = issuer;
    }

    public String generate(UserDetails user) {
        Instant now = Instant.now();
        List<String> roles = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).toList();

        JwtBuilder builder = Jwts.builder()
                .setSubject(user.getUsername())
                .claim("roles", roles)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(accessTtlMinutes * 60)))
                .signWith(key, SignatureAlgorithm.HS256);
        if (issuer != null && !issuer.isBlank()) builder.setIssuer(issuer);
        return builder.compact();
    }

    public Jws<Claims> parse(String token) {
        JwtParserBuilder parser = Jwts.parserBuilder().setSigningKey(key);
        if (issuer != null && !issuer.isBlank()) parser.requireIssuer(issuer);
        return parser.build().parseClaimsJws(token);
    }

    public String getUsername(String token) {
        return parse(token).getBody().getSubject();
    }

    public boolean isExpired(String token) {
        Date exp = parse(token).getBody().getExpiration();
        return exp.before(new Date());
    }

    public long getAccessTtlMinutes() {
        return accessTtlMinutes;
    }
}
