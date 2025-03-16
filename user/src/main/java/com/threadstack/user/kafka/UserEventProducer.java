package com.threadstack.user.kafka;

import org.springframework.kafka.core.KafkaTemplate;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.threadstack.user.kafka.dto.UserCreatedEvent;
import com.threadstack.user.model.User;
import com.threadstack.user.repository.FailedEventRepository;
import com.threadstack.user.model.FailedEvent;

@Service
public class UserEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final FailedEventRepository failedEventRepository;

    @Value("${spring.kafka.topic.user-created}")
    private String userCreatedTopic;

    public UserEventProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper,
	    FailedEventRepository failedEventRepository) {
	this.kafkaTemplate = kafkaTemplate;
	this.objectMapper = objectMapper;
	this.failedEventRepository = failedEventRepository;
    }

    public void sendUserCreatedEvent(User user) {
	try {
	    UserCreatedEvent event = new UserCreatedEvent(user.getId(), user.getUsername());
	    String eventJson = objectMapper.writeValueAsString(event);
	    kafkaTemplate.send(userCreatedTopic, eventJson).get(3, TimeUnit.SECONDS);
	} catch (JsonProcessingException e) {
	    queueFailedEvent(user.toString());
	} catch (Exception e) {
	    queueFailedEvent(user.toString());
	}
    }

    private void queueFailedEvent(String eventJson) {
	FailedEvent failedEvent = new FailedEvent(userCreatedTopic, eventJson);
	failedEventRepository.save(failedEvent)
        .doOnSuccess(savedEvent -> RetryStatus.shouldRetry.set(true))
        .subscribe();
    }
}
