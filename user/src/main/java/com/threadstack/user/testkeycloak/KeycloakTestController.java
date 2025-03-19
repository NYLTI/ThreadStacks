package com.threadstack.user.testkeycloak;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.threadstack.user.config.keycloak.KeycloakService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/test/keycloak")
@RequiredArgsConstructor
public class KeycloakTestController {

    private final KeycloakService keycloakService;

    @PostMapping("/create-user")
    public ResponseEntity<String> createTestUser(@RequestParam String username, 
                                                 @RequestParam String email,
                                                 @RequestParam String password) {
        try {
            keycloakService.createUser(username, email, password);
            return ResponseEntity.ok("User created successfully in Keycloak!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to create user: " + e.getMessage());
        }
    }
    
    @PostMapping("/create-role")
    public ResponseEntity<String> createTestRole(@RequestParam String roleName) {
        try {
            keycloakService.createRoleIfNotExists(roleName);
            return ResponseEntity.ok("Role '" + roleName + "' created successfully in Keycloak!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to create role: " + e.getMessage());
        }
    }
    
    @PostMapping("/assign-role")
    public ResponseEntity<String> assignRole(@RequestParam String username, @RequestParam String roleName) {
        try {
            keycloakService.assignRoleToUser(username, roleName);
            return ResponseEntity.ok("Role '" + roleName + "' assigned to user '" + username + "' successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to assign role: " + e.getMessage());
        }
    }
}

