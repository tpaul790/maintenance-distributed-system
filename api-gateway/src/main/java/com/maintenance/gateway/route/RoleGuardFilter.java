package com.maintenance.gateway.route;

import com.maintenance.gateway.security.AdminOnly;
import com.maintenance.gateway.security.RequireRoles;
import com.maintenance.gateway.security.Role;
import com.maintenance.gateway.security.RoleAuthorizationService;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.HandlerFilterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import java.lang.reflect.Method;
import java.util.Map;

/** Builds {@link HandlerFilterFunction}s that enforce the annotation found on a {@link RoutePolicies} method. */
@Component
public class RoleGuardFilter {

    private final RoleAuthorizationService authorizationService;

    public RoleGuardFilter(RoleAuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    public HandlerFilterFunction<ServerResponse, ServerResponse> forPolicy(String policyMethod) {
        Method method;
        try {
            method = RoutePolicies.class.getMethod(policyMethod);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Unknown route policy: " + policyMethod, e);
        }
        Role[] allowed = resolveRoles(method);

        return (request, next) -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            try {
                authorizationService.validateRoles(auth, allowed);
            } catch (AccessDeniedException e) {
                return ServerResponse.status(403)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(Map.of("status", 403, "error", "Forbidden", "message", e.getMessage()));
            }
            return next.handle(request);
        };
    }

    private Role[] resolveRoles(Method method) {
        if (method.isAnnotationPresent(AdminOnly.class)) {
            return new Role[]{Role.ADMIN};
        }
        RequireRoles req = method.getAnnotation(RequireRoles.class);
        if (req != null) {
            return req.value();
        }
        throw new IllegalStateException("Route policy " + method.getName() + " has no role annotation");
    }
}
