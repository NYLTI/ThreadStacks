package com.threadstack.thread.handler;

import com.threadstack.thread.exception.ResourceNotFoundException;
import com.threadstack.thread.model.Reply;
import com.threadstack.thread.repository.ReplyRepository;
import com.threadstack.thread.repository.ThreadRepository;
import com.threadstack.thread.util.ValidationUtil;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
    String userName = request.headers().firstHeader("X-User-Name");
    if (userName == null) {
      return ServerResponse.status(HttpStatus.UNAUTHORIZED).bodyValue("Missing X-User-Name header");
    }
    if (threadId == null && parentReplyId == null) {
      return ServerResponse.badRequest()
          .bodyValue("Either threadId or parentReply must be provided.");
    }
    return request.bodyToMono(Reply.class).flatMap(validationUtil::validate).flatMap(reply -> {
      reply.setUsername(userName);
      if (threadId != null) {
        return threadRepository.findById(threadId)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException("Thread not found")))
            .flatMap(thread -> {
              reply.setThreadId(threadId);
              return replyRepository.save(reply).flatMap(savedReply -> {
                thread.getReplies().add(savedReply.getId());
                return threadRepository.save(thread).thenReturn(savedReply);
              });
            });
      } else {
        return replyRepository.findById(parentReplyId)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException("Parent reply not found")))
            .flatMap(parent -> {
              reply.setParentReplyId(parentReplyId);
              reply.setThreadId(parent.getThreadId());
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
    } else if (parentReplyId != null) {
      return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
          .body(replyRepository.findByParentReplyId(parentReplyId), Reply.class);
    } else {
      return Mono.empty();
    }
  }

  public Mono<ServerResponse> getRepliesByParent(ServerRequest request) {
    String parentReplyId = request.pathVariable("parentReplyId");
    return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
        .body(replyRepository.findByParentReplyId(parentReplyId), Reply.class);
  }

  public Mono<ServerResponse> deleteReply(ServerRequest request) {
    String userName = request.headers().firstHeader("X-User-Name");
    String rolesHeader = request.headers().firstHeader("X-User-Roles");
    List<String> userRoles =
        rolesHeader != null ? Arrays.asList(rolesHeader.split(",")) : Collections.emptyList();

    String replyId = request.pathVariable("replyId");
    if (userName == null) {
      return ServerResponse.status(HttpStatus.UNAUTHORIZED).bodyValue("Missing X-User-Name header");
    }

    if (replyId == null) {
      return ServerResponse.status(HttpStatus.BAD_REQUEST)
          .bodyValue("Missing replyId pathVariable");
    }

    return replyRepository.findById(replyId)
        .switchIfEmpty(Mono.error(new ResourceNotFoundException("Reply not found")))
        .flatMap(reply -> {
          return threadRepository.findById(reply.getThreadId())
              .switchIfEmpty(
                  Mono.error(new ResourceNotFoundException("Thread for this reply not found")))
              .flatMap(thread -> {
                String roomName = thread.getRoomName();
                boolean isModerator =
                    userRoles.stream().anyMatch(role -> role.equalsIgnoreCase(roomName));
                if (!userName.equals(reply.getUsername()) && !isModerator) {
                  return ServerResponse.status(HttpStatus.FORBIDDEN)
                      .bodyValue("You are not authorized to delete this reply.");
                }
                reply.setContent("[deleted]");
                reply.setUsername(null);
                return replyRepository.save(reply)
                    .then(ServerResponse.ok().bodyValue("Reply deleted"));
              });
        });
  }
}
