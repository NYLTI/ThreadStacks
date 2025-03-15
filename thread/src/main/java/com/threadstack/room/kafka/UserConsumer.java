package com.threadstack.room.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.threadstack.thread.model.User;
import com.threadstack.thread.repository.UserRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class UserConsumer {

    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public UserConsumer(UserRepository userRepository, ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "user-created-topic", groupId = "thread-service-group")
    public void consumeUser(String userJson) {
        System.out.println("Received Raw Kafka Event: " + userJson);

        try {
            User user = objectMapper.readValue(userJson, User.class);
            System.out.println("Parsed User: " + user);

            userRepository.save(user)
                .doOnSuccess(savedUser -> System.out.println("User saved to MongoDB: " + savedUser))
                .subscribe();

        } catch (JsonProcessingException e) {
            e.printStackTrace();
            System.err.println("Failed to parse Kafka event: " + userJson);
        }
    }
}
