package com.deloitte.API_Gateway.filter;

import com.deloitte.API_Gateway.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

/**
 * Gateway filter factory for Role-Based Access Control (RBAC).
 * This filter checks if the user's role (from JWT token) has access to the requested resource.
 */
@Component
public class RbacGatewayFilterFactory extends AbstractGatewayFilterFactory<RbacGatewayFilterFactory.Config> {

    private static final Logger logger = LoggerFactory.getLogger(RbacGatewayFilterFactory.class);

    @Autowired
    private JwtUtil jwtUtil;

    public RbacGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            
            // Get Authorization header
            String authHeader = request.getHeaders().getFirst("Authorization");
            
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                logger.warn("Missing or invalid Authorization header for path: {}", request.getPath());
                return onError(exchange, "Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED);
            }
            
            // Extract role from JWT token
            String role = jwtUtil.extractRole(authHeader);
            
            if (role == null) {
                logger.warn("No role found in JWT token for path: {}", request.getPath());
                return onError(exchange, "No role found in token", HttpStatus.FORBIDDEN);
            }
            
            logger.debug("Extracted role: {} for path: {}", role, request.getPath());
            
            // Check if the role is allowed
            if (!isRoleAllowed(role, config.getAllowedRoles())) {
                logger.warn("Role {} is not allowed to access path: {}", role, request.getPath());
                return onError(exchange, "Access denied for role: " + role, HttpStatus.FORBIDDEN);
            }
            
            logger.info("Access granted for role {} to path: {}", role, request.getPath());
            
            // Add role to request header for downstream services
            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-User-Role", role)
                    .build();
            
            return chain.filter(exchange.mutate().request(modifiedRequest).build());
        };
    }

    /**
     * Checks if the given role is in the list of allowed roles.
     */
    private boolean isRoleAllowed(String role, List<String> allowedRoles) {
        if (allowedRoles == null || allowedRoles.isEmpty()) {
            // If no roles specified, allow all authenticated users
            return true;
        }
        
        return allowedRoles.stream()
                .anyMatch(allowedRole -> allowedRole.equalsIgnoreCase(role));
    }

    /**
     * Handles error responses.
     */
    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        logger.error("Error: {}", err);
        return response.setComplete();
    }

    /**
     * Configuration class for RBAC filter.
     */
    public static class Config {
        private List<String> allowedRoles;

        public List<String> getAllowedRoles() {
            return allowedRoles;
        }

        public void setAllowedRoles(List<String> allowedRoles) {
            this.allowedRoles = allowedRoles;
        }

        public void setAllowedRoles(String roles) {
            this.allowedRoles = Arrays.stream(roles.split(","))
                    .map(String::trim)
                    .toList();
        }
    }
}


