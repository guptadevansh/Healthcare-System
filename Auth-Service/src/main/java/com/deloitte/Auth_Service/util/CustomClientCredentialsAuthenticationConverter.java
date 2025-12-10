package com.deloitte.Auth_Service.util;

import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientCredentialsAuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.util.StringUtils;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * Custom converter that extracts the "role" parameter from the token request
 * and includes it in the authentication token's additional parameters.
 */
public class CustomClientCredentialsAuthenticationConverter implements AuthenticationConverter {

    @Nullable
    @Override
    public Authentication convert(HttpServletRequest request) {
        // Only process client_credentials grant type
        String grantType = request.getParameter(OAuth2ParameterNames.GRANT_TYPE);
        if (!AuthorizationGrantType.CLIENT_CREDENTIALS.getValue().equals(grantType)) {
            return null;
        }

        // Get the authenticated client
        Authentication clientPrincipal = SecurityContextHolder.getContext().getAuthentication();

        // Extract additional parameters including "role"
        Map<String, Object> additionalParameters = new HashMap<>();
        
        request.getParameterMap().forEach((key, value) -> {
            if (!key.equals(OAuth2ParameterNames.GRANT_TYPE) &&
                !key.equals(OAuth2ParameterNames.CLIENT_ID) &&
                !key.equals(OAuth2ParameterNames.CLIENT_SECRET) &&
                !key.equals(OAuth2ParameterNames.SCOPE)) {
                
                // Add custom parameters (like "role") to additional parameters
                additionalParameters.put(key, (value.length > 0) ? value[0] : "");
            }
        });

        // Extract scopes if present
        String scope = request.getParameter(OAuth2ParameterNames.SCOPE);
        java.util.Set<String> requestedScopes = null;
        if (StringUtils.hasText(scope)) {
            requestedScopes = new java.util.HashSet<>(
                java.util.Arrays.asList(StringUtils.delimitedListToStringArray(scope, " ")));
        }

        // Create authentication token with additional parameters
        return new OAuth2ClientCredentialsAuthenticationToken(
            clientPrincipal,
            requestedScopes,
            additionalParameters
        );
    }
}

