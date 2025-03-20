package com.threadstack.user.auth;

import com.threadstack.user.config.keycloak.KeycloakProvider;
//import com.threadstack.user.repository.UserRepository;
//import com.threadstack.user.security.JwtUtil;

import org.keycloak.representations.AccessTokenResponse;
//import org.keycloak.representations.AccessTokenResponse;
//import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/auth")
public class AuthController {
    
    private final KeycloakProvider keycloakProvider;
    
    public AuthController (KeycloakProvider keycloakProvider) {
	this.keycloakProvider = keycloakProvider;
    }
    
    @PostMapping("/login")
    public Mono<ResponseEntity<AccessTokenResponse>> login(@RequestBody AuthRequest request) {
        return keycloakProvider.getKeycloakToken(request.getUsername(), request.getPassword())
                .map(response -> ResponseEntity.ok(response))
                .onErrorResume(error -> Mono.just(ResponseEntity.status(401).build()));
    }

}
