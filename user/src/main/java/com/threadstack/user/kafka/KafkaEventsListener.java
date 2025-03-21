package com.threadstack.user.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.threadstack.user.config.keycloak.KeycloakService;
import com.threadstack.user.model.RoomCreatedEvent;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class KafkaEventsListener {
    private final ObjectMapper objectMapper;
    private final KeycloakService keycloakService;
    
    
    @KafkaListener(topics = "room-created-topic", groupId = "user-service-group")
    public void consumerRoomCreation(String roomJson) {
	try {
	    RoomCreatedEvent roomCreatedEvent = objectMapper.readValue(roomJson, RoomCreatedEvent.class);	    
	    keycloakService.assignRoleToUser(roomCreatedEvent.getUsername(), roomCreatedEvent.getRoomName());
	    keycloakService.assignRoleToUser(roomCreatedEvent.getUsername(), "CREATOR");
	}catch( Exception e) {
	    System.err.println(e.getMessage());
	}
    }
    
    @KafkaListener(topics = "moderator-created-topic", groupId = "user-service-group")
    public void consumerModeratorCreation(String roomJson) {
	try {
	    RoomCreatedEvent roomCreatedEvent = objectMapper.readValue(roomJson, RoomCreatedEvent.class);	    
	    keycloakService.assignRoleToUser(roomCreatedEvent.getUsername(), roomCreatedEvent.getRoomName());
	}catch( Exception e) {
	    System.err.println(e.getMessage());
	}
    }
}
