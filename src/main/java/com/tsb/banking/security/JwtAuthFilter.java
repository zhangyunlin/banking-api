package com.tsb.banking.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    /** Do not run this filter for public endpoints and preflight */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String p = request.getServletPath();
        return "OPTIONS".equals(request.getMethod())
                || p.startsWith("/auth/")
                || p.startsWith("/swagger")
                || p.startsWith("/v3/api-docs")
                || p.startsWith("/h2-console");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String h = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (h == null || !h.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        String token = h.substring(7).trim();

        try {
            // Do not allow refresh tokens to call protected APIs
            if (jwtService.isRefreshToken(token)) {
                unauthorized(response, "refresh_token_not_allowed");
                return;
            }

            // Parse & validate JWT, then load user
            String username = jwtService.getUsername(token); // throws JwtException if invalid
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails user = userDetailsService.loadUserByUsername(username);
                if (jwtService.isExpired(token)) {
                    unauthorized(response, "token_expired");
                    return;
                }
                var auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }

            chain.doFilter(request, response);
        } catch (JwtException | UsernameNotFoundException ex) {
            unauthorized(response, "invalid_token");
        }
    }

    private void unauthorized(HttpServletResponse res, String msg) throws IOException {
        res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        res.setContentType("application/json");
        res.getWriter().write("{\"error\":\"" + msg + "\"}");
    }
}
