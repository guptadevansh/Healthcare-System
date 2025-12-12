package com.deloitte.API_Gateway.util;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for JWT token operations.
 * Handles token parsing and role extraction.
 */
@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    /**
     * Extracts the role from the JWT token.
     */
    public String extractRole(String token) {
        try {
            // Remove "Bearer " prefix if present
            String jwtToken = token.replace("Bearer ", "").trim();
            
            // Parse the JWT token
            JWT jwt = JWTParser.parse(jwtToken);
            
            // Get the role claim
            Object roleClaim = jwt.getJWTClaimsSet().getClaim("role");
            
            if (roleClaim != null) {
                return roleClaim.toString();
            }
            
            logger.warn("No role claim found in JWT token");
            return null;
            
        } catch (Exception e) {
            logger.error("Error extracting role from JWT token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Extracts all roles from the JWT token.
     * Supports both single role (string) and multiple roles (list).
     */
    public List<String> extractRoles(String token) {
        List<String> roles = new ArrayList<>();
        
        try {
            // Remove "Bearer " prefix if present
            String jwtToken = token.replace("Bearer ", "").trim();
            
            // Parse the JWT token
            JWT jwt = JWTParser.parse(jwtToken);
            
            // Get the role claim
            Object roleClaim = jwt.getJWTClaimsSet().getClaim("role");
            
            if (roleClaim != null) {
                if (roleClaim instanceof List) {
                    // Multiple roles
                    for (Object role : (List<?>) roleClaim) {
                        roles.add(role.toString());
                    }
                } else {
                    // Single role
                    roles.add(roleClaim.toString());
                }
            }
            
            // Also check for "roles" claim (plural)
            Object rolesClaim = jwt.getJWTClaimsSet().getClaim("roles");
            if (rolesClaim instanceof List) {
                for (Object role : (List<?>) rolesClaim) {
                    if (!roles.contains(role.toString())) {
                        roles.add(role.toString());
                    }
                }
            }
            
        } catch (Exception e) {
            logger.error("Error extracting roles from JWT token: {}", e.getMessage());
        }
        return roles;
    }

    /**
     * Extracts the subject (user ID) from the JWT token.
     */
    public String extractSubject(String token) {
        try {
            // Remove "Bearer " prefix if present
            String jwtToken = token.replace("Bearer ", "").trim();
            
            // Parse the JWT token
            JWT jwt = JWTParser.parse(jwtToken);
            
            return jwt.getJWTClaimsSet().getSubject();
            
        } catch (Exception e) {
            logger.error("Error extracting subject from JWT token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Extracts the client ID from the JWT token.
     */
    public String extractClientId(String token) {
        try {
            // Remove "Bearer " prefix if present
            String jwtToken = token.replace("Bearer ", "").trim();
            
            // Parse the JWT token
            JWT jwt = JWTParser.parse(jwtToken);
            
            // Get the client_id claim
            Object clientIdClaim = jwt.getJWTClaimsSet().getClaim("client_id");
            
            if (clientIdClaim != null) {
                return clientIdClaim.toString();
            }
            
            return null;
            
        } catch (Exception e) {
            logger.error("Error extracting client_id from JWT token: {}", e.getMessage());
            return null;
        }
    }
}


