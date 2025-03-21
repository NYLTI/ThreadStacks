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
        try {
            User user = objectMapper.readValue(userJson, User.class);
            userRepository.save(user)
                .subscribe();

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
