package com.maintenance.gateway.security;

import com.maintenance.gateway.config.GatewayProperties;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Reads the {@code jwt_token} cookie, validates it and populates the SecurityContext.
 * An absent/invalid token simply leaves the request anonymous; the entry point decides the response.
 */
public class JwtCookieAuthenticationFilter extends OncePerRequestFilter {

    private final JwtValidator jwtValidator;
    private final String cookieName;

    public JwtCookieAuthenticationFilter(JwtValidator jwtValidator, GatewayProperties props) {
        this.jwtValidator = jwtValidator;
        this.cookieName = props.jwt().cookieName();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        extractToken(request)
                .flatMap(jwtValidator::parse)
                .ifPresent(this::authenticate);
        chain.doFilter(request, response);
    }

    private java.util.Optional<String> extractToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return java.util.Optional.empty();
        }
        for (Cookie cookie : cookies) {
            if (cookieName.equals(cookie.getName()) && !cookie.getValue().isBlank()) {
                return java.util.Optional.of(cookie.getValue());
            }
        }
        return java.util.Optional.empty();
    }

    private void authenticate(Claims claims) {
        String email = claims.getSubject();
        String role = claims.get("role", String.class);
        if (email == null || role == null) {
            return;
        }
        var auth = UsernamePasswordAuthenticationToken.authenticated(
                email, null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
