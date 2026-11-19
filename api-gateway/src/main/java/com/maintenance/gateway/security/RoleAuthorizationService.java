package com.maintenance.gateway.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RoleAuthorizationService {

    /** Tenant Management Service: ADMIN only. */
    public void validateTenantServiceAccess(Authentication authentication) {
        validateRoles(authentication, Role.ADMIN);
    }

    /** Utility & Billing Service: ADMIN and TENANT. */
    public void validateUtilityServiceAccess(Authentication authentication) {
        validateRoles(authentication, Role.ADMIN, Role.TENANT);
    }

    public void validateRoles(Authentication authentication, Role... allowed) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Authentication required");
        }
        Set<String> granted = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        boolean ok = Arrays.stream(allowed).anyMatch(r -> granted.contains("ROLE_" + r.name()));
        if (!ok) {
            throw new AccessDeniedException("Insufficient role for this resource");
        }
    }
}
