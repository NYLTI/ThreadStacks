package com.threadstack.thread.router;

import com.threadstack.thread.handler.ThreadHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.RouterFunctions.nest;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.DELETE;
import static org.springframework.web.reactive.function.server.RequestPredicates.path;

@Configuration
public class ThreadRouter {

    @Bean
    public RouterFunction<ServerResponse> threadRoutes(ThreadHandler handler) {
        return nest(path("/threads"),
                route(POST("").and(accept(MediaType.APPLICATION_JSON)), handler::createThread)
                        .andRoute(GET("/{threadId}"), handler::getThreadById)
//                        .andRoute(PUT("/{threadId}"), handler::updateThread)
                        .andRoute(DELETE("/{threadId}"), handler::deleteThread));
    }
}
