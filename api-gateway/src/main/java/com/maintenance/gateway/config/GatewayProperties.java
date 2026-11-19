package com.maintenance.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "gateway")
public record GatewayProperties(Routes routes, String frontendUrl, Jwt jwt) {

    public record Routes(String authServiceUrl, String tenantServiceUrl, String utilityServiceUrl) {
    }

    public record Jwt(String secret, String cookieName) {
    }
}
