package com.threadstack.room.router;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.threadstack.room.handler.RoomHandler;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.RouterFunctions.nest;
import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.PUT;
import static org.springframework.web.reactive.function.server.RequestPredicates.DELETE;

@Configuration
public class RoomRouter {

    @Bean
    public RouterFunction<ServerResponse> roomRoutes(RoomHandler handler) {
	return nest(path("/rooms"),
		route(POST("").and(accept(MediaType.APPLICATION_JSON)), handler::createRoom)
			.andRoute(GET("/{roomId}"), handler::getRoomById)
			.andRoute(GET("/{roomId}/members"), handler::getRoomMembers)
			.andRoute(GET("/{roomId}/owner"), handler::getRoomCreator)
			.andRoute(PUT("/{roomId}/members/{memberId}"), handler::addMember)
			.andRoute(DELETE("/{roomId}"), handler::deleteRoom)
			.andRoute(DELETE("/{roomId}/members/{memberId}"), handler::removeMember));
    }
}
