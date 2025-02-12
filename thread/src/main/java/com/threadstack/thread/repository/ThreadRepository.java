package com.threadstack.thread.repository;

import com.threadstack.thread.model.Thread;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface ThreadRepository extends ReactiveMongoRepository<Thread, String> {

    Flux<Thread> findByRoomId(String roomId);

    Flux<Thread> findByAuthorId(Long authorId);
}
