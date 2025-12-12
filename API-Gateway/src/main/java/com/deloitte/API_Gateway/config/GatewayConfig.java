package com.deloitte.API_Gateway.config;

import com.deloitte.API_Gateway.filter.RbacGatewayFilterFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
/**
 * Gateway routing configuration.
 * Defines routes to backend services with RBAC.
 */
@Configuration
public class GatewayConfig {

    @Value("${backend.userservice.url}")
    private String userServiceUrl;

    @Autowired
    private RbacGatewayFilterFactory rbacFilter;

    /**
     * Configures routes for the API Gateway.
     * Each route can have its own RBAC rules based on roles.
     */
    @Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                
                // User Service Routes
                
                // Admin-only routes - user management
                .route("user_service_admin", r -> r
                        .path("/api/users/admin/**")
                        .filters(f -> {
                            RbacGatewayFilterFactory.Config config = new RbacGatewayFilterFactory.Config();
                            config.setAllowedRoles("admin");
                            return f.stripPrefix(0)
                                    .filter(rbacFilter.apply(config));
                        })
                        .uri(userServiceUrl)
                )
                
                // Admin-only route - assign role to users
                .route("user_service_assign_role", r -> r
                        .path("/api/users/*/assignRole")
                        .filters(f -> {
                            RbacGatewayFilterFactory.Config config = new RbacGatewayFilterFactory.Config();
                            config.setAllowedRoles("admin");
                            return f.stripPrefix(0)
                                    .filter(rbacFilter.apply(config));
                        })
                        .uri(userServiceUrl)
                )
                
                // Doctor-accessible routes - view users
                .route("user_service_doctor", r -> r
                        .path("/api/users/doctors/**")
                        .filters(f -> {
                            RbacGatewayFilterFactory.Config config = new RbacGatewayFilterFactory.Config();
                            config.setAllowedRoles("admin,doctor");
                            return f.stripPrefix(0)
                                    .filter(rbacFilter.apply(config));
                        })
                        .uri(userServiceUrl)
                )
                
                // Patient-accessible routes - own profile
                .route("user_service_patient", r -> r
                        .path("/api/users/patients/**")
                        .filters(f -> {
                            RbacGatewayFilterFactory.Config config = new RbacGatewayFilterFactory.Config();
                            config.setAllowedRoles("admin,patient");
                            return f.stripPrefix(0)
                                    .filter(rbacFilter.apply(config));
                        })
                        .uri(userServiceUrl)
                )
                
                // General user routes - accessible by all authenticated users
                .route("user_service_general", r -> r
                        .path("/api/users/**")
                        .filters(f -> {
                            RbacGatewayFilterFactory.Config config = new RbacGatewayFilterFactory.Config();
                            config.setAllowedRoles("admin,doctor,patient");
                            return f.stripPrefix(0)
                                    .filter(rbacFilter.apply(config));
                        })
                        .uri(userServiceUrl)
                )
                .build();
    }
}


