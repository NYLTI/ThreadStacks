package com.threadstack.thread.router;

import com.threadstack.thread.handler.ReplyHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.RouterFunctions.nest;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration
public class ReplyRouter {

    @Bean
    public RouterFunction<ServerResponse> replyRoutes(ReplyHandler handler) {
        return nest(path("/replies"),
                route(POST("").and(accept(MediaType.APPLICATION_JSON)), handler::createReply)
                        .andRoute(DELETE("/{replyId}"), handler::deleteReply)
                        .andRoute(GET(""), handler::getRepliesByThreadIdorParentReplyId));
    }
}
