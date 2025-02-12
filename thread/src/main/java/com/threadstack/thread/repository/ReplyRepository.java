package com.threadstack.thread.repository;

import com.threadstack.thread.model.Reply;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface ReplyRepository extends ReactiveMongoRepository<Reply, String> {

    Flux<Reply> findByThreadId(String threadId);

    Flux<Reply> findByParentReplyId(String parentReplyId);
}
