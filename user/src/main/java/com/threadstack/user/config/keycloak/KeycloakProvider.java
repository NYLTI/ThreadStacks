package com.threadstack.user.config.keycloak;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.MediaType;


import reactor.core.publisher.Mono;

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
    
    private final WebClient webClient;

    public KeycloakProvider() {
        this.webClient = WebClient.builder().build();
    }

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

    public AccessTokenResponse getAccessToken() {
        try {
            return getKeycloakInstance().tokenManager().getAccessToken();
        } catch (Exception e) {
            throw new RuntimeException("Keycloak access token retrieval failed.");
        }
    }
    
    public Mono<AccessTokenResponse> getKeycloakToken(String username, String password) {
        return webClient.post()
            .uri(serverUrl + "/realms/" + realm + "/protocol/openid-connect/token")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(BodyInserters.fromFormData("client_id", clientId)
                .with("client_secret", clientSecret)
                .with("grant_type", "password")
                .with("username", username)
                .with("password", password))
            .retrieve()
            .bodyToMono(AccessTokenResponse.class);
    }
}
