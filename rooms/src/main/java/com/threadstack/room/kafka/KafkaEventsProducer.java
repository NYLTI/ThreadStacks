package com.threadstack.room.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.threadstack.room.model.FailedKafkaEvent;
import com.threadstack.room.repository.FailedKafkaEventRepository;
import com.threadstack.room.util.RetryUtility;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class KafkaEventsProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final FailedKafkaEventRepository failedKafkaEventRepository;

    @Value("${spring.kafka.topic.room-created}")
    private String roomCreatedTopic;
    
    @Value("${spring.kafka.topic.moderator-created}")
    private String moderatorCreatedTopic;

    public void sendRoomCreatedEvent(FailedKafkaEvent event) {
	try {
	    String eventJson = objectMapper.writeValueAsString(event);
	    kafkaTemplate.send(roomCreatedTopic, eventJson).get(3, TimeUnit.SECONDS);
	    System.out.println("RoomCreatedEvent sent for: " + event.getRoomName());
	} catch (JsonProcessingException e) {
	    queueFailedEvent(event.toString());
	} catch (Exception e) {
	    queueFailedEvent(event.toString());
	}
    }
    
    public void moderatorRoomCreatedEvent(FailedKafkaEvent event) {
	try {
	    String eventJson = objectMapper.writeValueAsString(event);
	    kafkaTemplate.send(moderatorCreatedTopic, eventJson).get(3, TimeUnit.SECONDS);
	    System.out.println("ModeratorCreatedEvent sent for: " + event.getRoomName());
	} catch (JsonProcessingException e) {
	    queueFailedEvent(event.toString());
	} catch (Exception e) {
	    queueFailedEvent(event.toString());
	}
    }

    private void queueFailedEvent(String eventJson) {
	FailedKafkaEvent failedKafkaEvent = new FailedKafkaEvent(roomCreatedTopic, eventJson);
	failedKafkaEventRepository.save(failedKafkaEvent).doOnSuccess(saved -> RetryUtility.SHOULDRETRYKAFKA.set(true))
		.subscribe();
    }
}
