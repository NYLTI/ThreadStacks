package com.threadstack.user.config.keycloak;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class KeycloakProvider {

    @Value("${keycloak.server-url}")
    private String serverUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.client-secret}")
    private String clientSecret;

    @Value("${keycloak.admin-username}")
    private String adminUsername;

    @Value("${keycloak.admin-password}")
    private String adminPassword;

    private Keycloak keycloakInstance = null;

    /**
     * Lazily initializes and returns a Keycloak instance.
     * Ensures Keycloak is not instantiated at startup to avoid dependency issues.
     */
    public Keycloak getKeycloakInstance() {
        if (keycloakInstance == null) {
            keycloakInstance = KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(realm)
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .build();
        }
        return keycloakInstance;
    }

    /**
     * Retrieves an access token from Keycloak.
     * This method is called only when necessary, preventing startup failures if Keycloak is unavailable.
     */
    public AccessTokenResponse getAccessToken() {
        try {
            return getKeycloakInstance().tokenManager().getAccessToken();
        } catch (Exception e) {
            System.err.println("Failed to fetch access token: " + e.getMessage());
            throw new RuntimeException("Keycloak access token retrieval failed.");
        }
    }
}
