package com.maintenance.gateway.route;

import com.maintenance.gateway.security.AdminOnly;
import com.maintenance.gateway.security.RequireRoles;
import com.maintenance.gateway.security.Role;

/**
 * Declarative access policy per downstream service. The annotations on these marker methods are
 * read by {@link RoleGuardFilter}; change the policy here and the route guard follows.
 */
public final class RoutePolicies {

    private RoutePolicies() {
    }

    @AdminOnly
    public static void tenantService() {
    }

    @RequireRoles({Role.ADMIN, Role.TENANT})
    public static void utilityService() {
    }
}
