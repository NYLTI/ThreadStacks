package com.threadstack.user.controller;

import com.threadstack.user.dto.UserDTO;
import com.threadstack.user.model.User;
import com.threadstack.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public Mono<ResponseEntity<UserDTO>> registerUser(@Valid @RequestBody User user) {
        return userService.registerUser(user)
                .map(savedUser -> ResponseEntity.status(HttpStatus.CREATED).body(savedUser));
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<UserDTO>> getUserById(@PathVariable Long id) {
        return userService.findById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/email/{email}")
    public Mono<ResponseEntity<UserDTO>> getUserByEmail(@PathVariable String email) {
        return userService.findByEmail(email)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
