package com.threadstack.user.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Table("failed_keycloak_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FailedKeycloakEvent {

    @Id
    private Long id;
    private String username;
    private String roleName;
    private String password;
    private String eventType;
    private String email;
    private LocalDateTime createdAt = LocalDateTime.now();

}
