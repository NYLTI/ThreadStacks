package com.threadstack.thread.handler;

import com.threadstack.thread.exception.ResourceNotFoundException;
import com.threadstack.thread.model.Reply;
import com.threadstack.thread.repository.ReplyRepository;
import com.threadstack.thread.repository.ThreadRepository;
import com.threadstack.thread.util.ValidationUtil;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;

@Component
public class ReplyHandler {

    private final ReplyRepository replyRepository;
    private final ValidationUtil validationUtil;
    private final ThreadRepository threadRepository;

    public ReplyHandler(ReplyRepository replyRepository, ValidationUtil validationUtil,
	    ThreadRepository threadRepository) {
	this.replyRepository = replyRepository;
	this.validationUtil = validationUtil;
	this.threadRepository = threadRepository;
    }

    public Mono<ServerResponse> createReply(ServerRequest request) {
	String threadId = request.queryParam("threadId").orElse(null);
	String parentReplyId = request.queryParam("parentReplyId").orElse(null);

	if (threadId == null && parentReplyId == null) {
	    return ServerResponse.badRequest().bodyValue("Either threadId or parentReply must be provided.");
	}

	return request.bodyToMono(Reply.class).flatMap(validationUtil::validate).flatMap(reply -> {
	    if (threadId != null) {
		return threadRepository.findById(threadId)
			.switchIfEmpty(Mono.error(new ResourceNotFoundException("Thread not found")))
			.flatMap(thread -> {
			    reply.setThreadId(threadId);
			    return replyRepository.save(reply).flatMap(savedReply -> {
				thread.getReplies().add(reply.getId());
				return threadRepository.save(thread).thenReturn(savedReply);
			    });
			});

	    } else {
		return replyRepository.findById(parentReplyId)
			.switchIfEmpty(Mono.error(new ResourceNotFoundException("Parent reply not found")))
			.flatMap(parent -> {
			    reply.setParentReplyId(parentReplyId);
			    return replyRepository.save(reply).flatMap(savedReply -> {
				parent.getReplies().add(savedReply.getId());
				return replyRepository.save(parent).thenReturn(savedReply);
			    });
			});
	    }
	}).flatMap(savedReply -> ServerResponse.created(URI.create("/replies/" + savedReply.getId()))
		.contentType(MediaType.APPLICATION_JSON).bodyValue(savedReply));

    }

    public Mono<ServerResponse> getRepliesByThreadIdorParentReplyId(ServerRequest request) {
	String threadId = request.queryParam("threadId").orElse(null);
	String parentReplyId = request.queryParam("parentReplyId").orElse(null);

	if (threadId != null) {
	    return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
		    .body(replyRepository.findByThreadId(threadId), Reply.class);
	}else if (parentReplyId != null) {
	    return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
		    .body(replyRepository.findByParentReplyId(parentReplyId), Reply.class);
	}else {
	    return Mono.empty();
	}
    }

    public Mono<ServerResponse> getRepliesByParent(ServerRequest request) {
	String parentReplyId = request.pathVariable("parentReplyId");
	return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
		.body(replyRepository.findByParentReplyId(parentReplyId), Reply.class);
    }

    public Mono<ServerResponse> deleteReply(ServerRequest request) {
	String replyId = request.pathVariable("replyId");

	return replyRepository.findById(replyId)
		.switchIfEmpty(Mono.error(new ResourceNotFoundException("Reply not found"))).flatMap(reply -> {
		    reply.setContent("[deleted]");
		    return replyRepository.save(reply);
		}).flatMap(updatedReply -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
			.bodyValue(updatedReply));
    }

}
