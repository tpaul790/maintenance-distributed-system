package com.maintenance.gateway.route;

import com.maintenance.gateway.config.GatewayProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import static org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions.uri;
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;
import static org.springframework.web.servlet.function.RequestPredicates.path;

@Configuration
public class GatewayRoutesConfig {

    @Bean
    RouterFunction<ServerResponse> authRoute(GatewayProperties props) {
        return route("auth-service")
                .route(path("/api/auth/**"), http())
                .before(uri(props.routes().authServiceUrl()))
                .build();
    }

    @Bean
    RouterFunction<ServerResponse> tenantRoute(GatewayProperties props, RoleGuardFilter guard) {
        return route("tenant-service")
                .route(path("/api/tenants/**"), http())
                .before(uri(props.routes().tenantServiceUrl()))
                .filter(guard.forPolicy("tenantService"))
                .build();
    }

    @Bean
    RouterFunction<ServerResponse> utilityRoute(GatewayProperties props, RoleGuardFilter guard) {
        return route("utility-service")
                .route(path("/api/utilities/**"), http())
                .before(uri(props.routes().utilityServiceUrl()))
                .filter(guard.forPolicy("utilityService"))
                .build();
    }
}
