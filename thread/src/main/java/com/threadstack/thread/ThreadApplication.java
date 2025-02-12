package com.threadstack.thread;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

@SpringBootApplication
@EnableReactiveMongoRepositories("com")
@EntityScan("com")
@ComponentScan("com")
@EnableDiscoveryClient
public class ThreadApplication {

	public static void main(String[] args) {
		SpringApplication.run(ThreadApplication.class, args);
	}

}
