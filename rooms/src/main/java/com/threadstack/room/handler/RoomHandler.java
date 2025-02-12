package com.threadstack.room.handler;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.threadstack.room.exception.RoomNameAlreadyExistsException;
import com.threadstack.room.model.Room;
import com.threadstack.room.repository.RoomRepository;
import com.threadstack.room.util.ValidationUtil;

import reactor.core.publisher.Mono;

@Component
public class RoomHandler {
    private final RoomRepository roomRepository;
    private final ValidationUtil validationUtil;

    public RoomHandler(RoomRepository roomRepository, ValidationUtil validationUtil) {
	this.roomRepository = roomRepository;
	this.validationUtil = validationUtil;
    }

    public Mono<ServerResponse> createRoom(ServerRequest request) {
	return request.bodyToMono(Room.class).flatMap(validationUtil::validate).flatMap(room -> checkRoomExists(room))
		.flatMap(room -> {
		    room.getMembers().add(room.getCreatedBy());
		    room.getModerators().add(room.getCreatedBy());
		    return roomRepository.save(room);
		}).flatMap(savedRoom -> ServerResponse.created(request.uri()).contentType(MediaType.APPLICATION_JSON)
			.bodyValue(savedRoom));
    }

    public Mono<ServerResponse> getRoomById(ServerRequest request) {
	String roomId = request.pathVariable("roomId");
	return roomRepository.findById(roomId)
		.flatMap(room -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(room))
		.switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> addMember(ServerRequest request) {
	String roomId = request.pathVariable("roomId");
	Long memberId = Long.parseLong(request.pathVariable("memberId"));

	return roomRepository.addMember(roomId, memberId)
		.flatMap(room -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(room))
		.switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> removeMember(ServerRequest request) {
	String roomId = request.pathVariable("roomId");
	Long memberId = Long.parseLong(request.pathVariable("memberId"));

	return roomRepository.removeMember(roomId, memberId)
		.flatMap(room -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(room))
		.switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> deleteRoom(ServerRequest request) {
	String roomId = request.pathVariable("roomId");

	return roomRepository.findById(roomId)
		.flatMap(existingRoom -> roomRepository.deleteById(roomId).then(ServerResponse.noContent().build()))
		.switchIfEmpty(ServerResponse.status(HttpStatus.NOT_FOUND)
			.bodyValue(Map.of("error", "Room not found", "roomId", roomId)));
    }

    public Mono<ServerResponse> getRoomMembers(ServerRequest request) {
	String roomId = request.pathVariable("roomId");

	return roomRepository.findById(roomId).flatMap(
		room -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(room.getMembers()))
		.switchIfEmpty(
			ServerResponse.status(HttpStatus.NOT_FOUND).bodyValue(Map.of("error", "Room not found")));
    }

    public Mono<ServerResponse> getRoomCreator(ServerRequest request) {
	String roomId = request.pathVariable("roomId");

	return roomRepository.findById(roomId)
		.flatMap(room -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
			.bodyValue(Map.of("createdBy", room.getCreatedBy())))
		.switchIfEmpty(
			ServerResponse.status(HttpStatus.NOT_FOUND).bodyValue(Map.of("error", "Room not found")));
    }

    private Mono<Room> checkRoomExists(Room room) {
	return roomRepository.findByNameIgnoreCase(room.getName())
		.flatMap(existing -> Mono.<Room>error(new RoomNameAlreadyExistsException("Room name already exists")))
		.switchIfEmpty(Mono.just(room));
    }
}
