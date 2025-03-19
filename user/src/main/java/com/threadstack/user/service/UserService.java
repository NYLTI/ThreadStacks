package com.threadstack.user.service;

import com.threadstack.user.config.keycloak.KeycloakService;
import com.threadstack.user.exception.EmailAlreadyExistsException;
import com.threadstack.user.exception.UsernameAlreadyExistsException;
import com.threadstack.user.kafka.UserEventProducer;
import com.threadstack.user.model.Role;
import com.threadstack.user.model.User;
import com.threadstack.user.model.UserDTO;
import com.threadstack.user.repository.UserRepository;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private UserEventProducer userEventProducer;
    private final KeycloakService keycloakService;
    
    @Autowired
    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, UserEventProducer userEventProducer, KeycloakService keycloakService) {
    	this.passwordEncoder = passwordEncoder;
    	this.userRepository = userRepository;
    	this.userEventProducer = userEventProducer;
    	this.keycloakService = keycloakService;
    }

    public Mono<UserDTO> registerUser(User user) {
        return userRepository.findByUsername(user.getUsername())
                .flatMap(existing -> Mono.error(new UsernameAlreadyExistsException("Username already exists")))
                .switchIfEmpty(userRepository.findByEmail(user.getEmail())
                        .flatMap(existing -> Mono.error(new EmailAlreadyExistsException("Email already exists"))))
                .then(Mono.defer(() -> {
                    user.setPassword(passwordEncoder.encode(user.getPassword()))
                    .setCreatedAt(LocalDateTime.now())
                    .setRole(Role.USER);
                    
                    return userRepository.save(user)
                            .doOnSuccess(savedUser ->{
                        	try {
                        	    userEventProducer.sendUserCreatedEvent(savedUser);
                        	    keycloakService.createUser(savedUser.getUsername(), savedUser.getEmail(), savedUser.getPassword());
                        	}
                        	catch(Exception ex){
                        	    
                        	}
                            })
                            .map(this::convertToDTO);
                }));
    }


    public Mono<UserDTO> findByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(this::convertToDTO);
    }

    public Mono<UserDTO> findById(Long id) {
        return userRepository.findById(id)
                .map(this::convertToDTO);
    }
    
    private UserDTO convertToDTO(User user) {
        return new UserDTO(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getRole(),
            user.getCreatedAt()
        );
    }

}
