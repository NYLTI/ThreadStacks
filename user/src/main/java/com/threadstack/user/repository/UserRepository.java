package com.threadstack.user.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

import com.threadstack.user.model.User;

import reactor.core.publisher.Mono;


@Repository
public interface UserRepository extends R2dbcRepository<User, Long> {
	Mono<User> findByEmail(String email);
	Mono<User> findByUsername(String username);
}
