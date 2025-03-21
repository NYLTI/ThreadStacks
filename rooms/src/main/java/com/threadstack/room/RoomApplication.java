package com.threadstack.room;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ComponentScan("com")
@EnableReactiveMongoRepositories("com")
@EntityScan("com")
@EnableScheduling
public class RoomApplication {

    public static void main(String[] args) {
	SpringApplication.run(RoomApplication.class, args);
    }

}
