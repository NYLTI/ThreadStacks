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
//    private final PasswordEncoder passwordEncoder;
    
    public AuthController (KeycloakProvider keycloakProvider) {
	this.keycloakProvider = keycloakProvider;
//	this.passwordEncoder = new BCryptPasswordEncoder();
    }

//    private final JwtUtil jwtUtil;
//    private final UserRepository userRepository;
//    private final PasswordEncoder passwordEncoder;
//    
//    @Autowired
//    public AuthController(JwtUtil jwtUtil, UserRepository userRepository) {
//        this.jwtUtil = jwtUtil;
//        this.userRepository = userRepository;
//        this.passwordEncoder = new BCryptPasswordEncoder();
//    }

//    @PostMapping("/login")
//    public Mono<ResponseEntity<AuthResponse>> login(@RequestBody AuthRequest request) {
//        return userRepository.findByUsername(request.getUsername())
//                .flatMap(user -> {
//                    if (passwordEncoder.matches(request.getPassword(), user.getPassword())) {
//                        String token = jwtUtil.generateToken(user.getId().toString(), user.getUsername(), user.getRole());
//                        return Mono.just(ResponseEntity.ok(new AuthResponse(token)));
//                    } else {
//                        return Mono.just(ResponseEntity.status(401).body(new AuthResponse(null)));
//                    }
//                })
//                .switchIfEmpty(Mono.just(ResponseEntity.status(401).body(new AuthResponse(null))));
//    }
    
    @PostMapping("/login")
    public Mono<ResponseEntity<AccessTokenResponse>> login(@RequestBody AuthRequest request) {
	System.out.println("✅ Received login request: " + request.getUsername());
        return keycloakProvider.getKeycloakToken(request.getUsername(), request.getPassword())
                .map(response -> ResponseEntity.ok(response))
                .onErrorResume(error -> Mono.just(ResponseEntity.status(401).build()));
    }

}
