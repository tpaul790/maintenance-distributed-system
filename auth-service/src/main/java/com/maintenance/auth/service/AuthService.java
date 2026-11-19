package com.maintenance.auth.service;

import com.maintenance.auth.dto.LoginRequest;
import com.maintenance.auth.entity.User;
import com.maintenance.auth.exception.InvalidCredentialsException;
import com.maintenance.auth.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    // Valid hash used to equalize timing when the email is unknown (avoids user enumeration)
    private static final String DUMMY_HASH = "$2a$10$ux9dp/IoieIrJtQDLge9se3EShyL5N8e3Q79CZZhlxgDCDgutZdNa";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public User authenticate(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.email().trim()).orElse(null);
        if (user == null) {
            passwordEncoder.matches(request.password(), DUMMY_HASH);
            throw new InvalidCredentialsException();
        }
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
        return user;
    }
}
