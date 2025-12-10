package com.deloitte.Auth_Service.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientCredentialsAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Custom token customizer that adds the "role" claim to the JWT access token.
 * The role value comes from the request parameters sent to /oauth2/token endpoint.
 */
@Component
public class CustomTokenCustomizer implements OAuth2TokenCustomizer<JwtEncodingContext> {

    @Override
    public void customize(JwtEncodingContext context) {
        if (context.getTokenType().getValue().equals("access_token")) {
            Authentication authorizationGrant = context.getAuthorizationGrant();
            if (authorizationGrant instanceof OAuth2ClientCredentialsAuthenticationToken) {
                OAuth2ClientCredentialsAuthenticationToken token = 
                    (OAuth2ClientCredentialsAuthenticationToken) authorizationGrant;
                
                Map<String, Object> additionalParameters = token.getAdditionalParameters();
                
                if (additionalParameters != null && !additionalParameters.isEmpty()) {
                    additionalParameters.forEach((key, value) -> {
                        if (value != null && !value.toString().isEmpty()) {
                            context.getClaims().claim(key, value);
                        }
                    });
                }
            }
            context.getClaims().claim("client_id", context.getPrincipal().getName());
        }
    }
}

