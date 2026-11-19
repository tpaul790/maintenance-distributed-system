package com.maintenance.auth.controller;

import com.maintenance.auth.config.AppProperties;
import com.maintenance.auth.dto.LoginRequest;
import com.maintenance.auth.dto.LoginResponse;
import com.maintenance.auth.entity.User;
import com.maintenance.auth.security.JwtService;
import com.maintenance.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;
    private final String cookieName;

    public AuthController(AuthService authService, JwtService jwtService, AppProperties props) {
        this.authService = authService;
        this.jwtService = jwtService;
        this.cookieName = props.cookie().name();
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        User user = authService.authenticate(request);
        String token = jwtService.generateToken(user);

        // Not HttpOnly on purpose: the SPA also writes/reads this cookie (JSON + cookie clients are both supported)
        ResponseCookie cookie = ResponseCookie.from(cookieName, token)
                .path("/")
                .sameSite("Lax")
                .maxAge(jwtService.getExpiration())
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new LoginResponse(token, user.getEmail(), user.getRole().name()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        ResponseCookie expired = ResponseCookie.from(cookieName, "")
                .path("/")
                .sameSite("Lax")
                .maxAge(Duration.ZERO)
                .build();
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, expired.toString())
                .build();
    }
}
