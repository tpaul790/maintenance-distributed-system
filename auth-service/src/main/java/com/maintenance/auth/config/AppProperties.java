package com.maintenance.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(Jwt jwt, Cookie cookie) {

    public record Jwt(String secret, long expirationMinutes) {
    }

    public record Cookie(String name) {
    }
}
