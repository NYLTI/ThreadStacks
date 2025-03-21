package com.threadstack.thread.handler;

import com.threadstack.thread.exception.ResourceNotFoundException;
import com.threadstack.thread.model.Thread;
import com.threadstack.thread.repository.ThreadRepository;
import com.threadstack.thread.util.ValidationUtil;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;

@Component
@RequiredArgsConstructor
public class ThreadHandler {

    private final ThreadRepository threadRepository;
    private final ValidationUtil validationUtil;

    public Mono<ServerResponse> createThread(ServerRequest request) {
	String userName = request.headers().firstHeader("X-User-Name");

	if (userName == null) {
	    return ServerResponse.status(HttpStatus.UNAUTHORIZED).bodyValue("Missing X-User-Name header");
	}

	return request.bodyToMono(Thread.class).flatMap(validationUtil::validate).map(thread -> {
	    thread.setUserName(userName);
	    return thread;
	}).flatMap(threadRepository::save)
		.flatMap(savedThread -> ServerResponse.created(URI.create("/threads/" + savedThread.getId()))
			.contentType(MediaType.APPLICATION_JSON).bodyValue(savedThread));
    }

    public Mono<ServerResponse> getThreadById(ServerRequest request) {
	String threadId = request.pathVariable("threadId");
	return threadRepository.findById(threadId)
		.flatMap(thread -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(thread))
		.switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> getThreadsByRoom(ServerRequest request) {
	String roomName = request.pathVariable("roomName");
	return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
		.body(threadRepository.findByRoomName(roomName), Thread.class);
    }

    public Mono<ServerResponse> deleteThread(ServerRequest request) {
	String userName = request.headers().firstHeader("X-User-Name");
	String threadId = request.pathVariable("threadId");
	if (userName == null) {
	    return ServerResponse.status(HttpStatus.UNAUTHORIZED).bodyValue("Missing X-User-Name header");
	}
	if (threadId == null) {
	    return ServerResponse.status(HttpStatus.BAD_REQUEST).bodyValue("Missing threadId pathVariable");
	}
	return threadRepository.findById(threadId)
		.switchIfEmpty(Mono.error(new ResourceNotFoundException("Thread not found"))).flatMap(thread -> {
		    if (!userName.equals(thread.getUserName())) {
			if (thread.getUserName() == null) {
			    return ServerResponse.status(HttpStatus.BAD_REQUEST)
				    .bodyValue("Thread has already been deleted");
			}
			return ServerResponse.status(HttpStatus.FORBIDDEN)
				.bodyValue("You are not the creator of this thread");
		    }
		    thread.setContent("[deleted]");
		    thread.setUserName(null);
		    return threadRepository.save(thread).then(ServerResponse.ok().bodyValue("Thread deleted"));
		});
    }

}
