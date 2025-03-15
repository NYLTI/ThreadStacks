package com.threadstack.user.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.threadstack.user.kafka.dto.UserCreatedEvent;
import com.threadstack.user.model.User;

@Service
public class UserEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private ObjectMapper objectMapper;
    
    @Value("${spring.kafka.topic.user-created}")
    private String userCreatedTopic;

    public UserEventProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void sendUserCreatedEvent(User user) {
        try {
            UserCreatedEvent event = new UserCreatedEvent(user.getId(), user.getUsername());
            String eventJson = objectMapper.writeValueAsString(event);

            kafkaTemplate.send(userCreatedTopic, eventJson);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
