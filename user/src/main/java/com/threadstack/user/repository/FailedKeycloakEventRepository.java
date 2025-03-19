package com.threadstack.user.repository;

import com.threadstack.user.model.FailedKeycloakEvent;

import java.util.Optional;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FailedKeycloakEventRepository extends R2dbcRepository<FailedKeycloakEvent, Long> {
    Flux<FailedKeycloakEvent> findAllByOrderByCreatedAtAsc();
    public Optional<Mono<FailedKeycloakEvent>> findByUsernameAndEventType(String username, String eventType);
    public Optional<Mono<FailedKeycloakEvent>> findByUsernameAndRoleName(String username, String role);
    public Optional<Mono<FailedKeycloakEvent>> findByRoleName(String role);
}
