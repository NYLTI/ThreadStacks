package com.threadstack.thread.handler;

import com.threadstack.thread.exception.ResourceNotFoundException;
import com.threadstack.thread.model.Thread;
import com.threadstack.thread.repository.ThreadRepository;
import com.threadstack.thread.util.ValidationUtil;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;

@Component
public class ThreadHandler {

    private final ThreadRepository threadRepository;
    private final ValidationUtil validationUtil;

    public ThreadHandler(ThreadRepository threadRepository, ValidationUtil validationUtil) {
        this.threadRepository = threadRepository;
        this.validationUtil = validationUtil;
    }

    public Mono<ServerResponse> createThread(ServerRequest request) {
        return request.bodyToMono(Thread.class)
                .flatMap(validationUtil::validate)
                .flatMap(threadRepository::save)
                .flatMap(savedThread -> ServerResponse.created(URI.create("/threads/" + savedThread.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(savedThread));
    }

    public Mono<ServerResponse> getThreadById(ServerRequest request) {
        String threadId = request.pathVariable("threadId");
        return threadRepository.findById(threadId)
                .flatMap(thread -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(thread))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> getThreadsByRoom(ServerRequest request) {
        String roomId = request.pathVariable("roomId");
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(threadRepository.findByRoomId(roomId), Thread.class);
    }
    
    public Mono<ServerResponse> deleteThread(ServerRequest request) {
	    String threadId = request.pathVariable("threadId");

	    return threadRepository.findById(threadId)
	            .switchIfEmpty(Mono.error(new ResourceNotFoundException("Thread not found")))
	            .flatMap(thread -> {
	                thread.setContent("[deleted]");
	                return threadRepository.save(thread);
	            })
	            .flatMap(updatedThread -> ServerResponse.ok()
	                    .contentType(MediaType.APPLICATION_JSON)
	                    .bodyValue(updatedThread));
	}


}
