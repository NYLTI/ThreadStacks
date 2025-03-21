package com.threadstack.room.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.threadstack.room.model.User;
import com.threadstack.room.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaEventsListener {

    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "user-created-topic", groupId = "room-service-group")
    public void consumeUser(String userJson) {
        try {
            User user = objectMapper.readValue(userJson, User.class);
            userRepository.save(user)
                .subscribe();

        } catch (JsonProcessingException e) {
            System.err.println(e.getMessage());
        }
    }
}
