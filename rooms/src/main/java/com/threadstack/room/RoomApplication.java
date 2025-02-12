package com.threadstack.room;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

@SpringBootApplication
@ComponentScan("com")
@EnableReactiveMongoRepositories
@EntityScan("com")
public class RoomApplication {

    public static void main(String[] args) {
	SpringApplication.run(RoomApplication.class, args);
    }

}
