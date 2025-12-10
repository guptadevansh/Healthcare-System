package com.deloitte.Auth_Service;

import org.springframework.lang.Nullable;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

import java.time.Duration;
import java.util.UUID;

/**
 * Custom RegisteredClientRepository that dynamically accepts any clientId
 * from the Basic Auth header in the /oauth2/token API request.
 */
public class DynamicClientRepository implements RegisteredClientRepository {

    @Override
    public void save(RegisteredClient registeredClient) {
    }

    @Override
    @Nullable
    public RegisteredClient findById(String id) {
        return null;
    }

    @Override
    @Nullable
    public RegisteredClient findByClientId(String clientId) {
        // Dynamically create and return a RegisteredClient for any clientId
        // The clientId comes from the Basic Auth header in the API request
        return RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId(clientId)
                .clientSecret("any")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofHours(1))
                        .build())
                .build();
    }
}

