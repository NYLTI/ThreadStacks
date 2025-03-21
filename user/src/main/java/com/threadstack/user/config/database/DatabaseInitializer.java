package com.threadstack.user.config.database;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

@Configuration
public class DatabaseInitializer {

    @Value("${spring.r2dbc.url}")
    private String jdbcUrl;

    @Value("${spring.r2dbc.username}")
    private String dbUser;

    @Value("${spring.r2dbc.password}")
    private String dbPassword;

    private static final String DATABASE_NAME = "threadstacks";

    @Bean
    public CommandLineRunner initializeDatabase() {
	return args -> {
	    String jdbcBaseUrl = jdbcUrl.replace("r2dbc:mysql://", "jdbc:mysql://").replace("/" + DATABASE_NAME, "");

	    try (Connection connection = DriverManager.getConnection(jdbcBaseUrl, dbUser, dbPassword);
		    Statement statement = connection.createStatement()) {

		// Create database if it doesn't exist
		statement.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DATABASE_NAME);

		// Switch to the database
		statement.executeUpdate("USE " + DATABASE_NAME);

		// Create users table if it doesn't exist
		String createTableSQL = """
			    CREATE TABLE IF NOT EXISTS users (
			        id BIGINT AUTO_INCREMENT PRIMARY KEY,
			        username VARCHAR(50) NOT NULL UNIQUE,
			        first_name VARCHAR(50) NOT NULL,
			        last_name VARCHAR(50) NOT NULL,
			        email VARCHAR(100) NOT NULL UNIQUE,
			        password VARCHAR(255) NOT NULL,
			        role ENUM('USER', 'MODERATOR', 'ADMIN') NOT NULL DEFAULT 'USER',
			        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
			    );
			""";
		statement.executeUpdate(createTableSQL);

		String createFailedKafkaEventsTableSQL = """
			    CREATE TABLE IF NOT EXISTS failed_kafka_events (
			        id BIGINT AUTO_INCREMENT PRIMARY KEY,
			        topic VARCHAR(255) NOT NULL,
			        event_data TEXT NOT NULL,
			        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
			    );
			""";
		statement.executeUpdate(createFailedKafkaEventsTableSQL);

		String createFailedKeycloakEventsTableSQL = """
			    CREATE TABLE IF NOT EXISTS failed_keycloak_events (
			        id BIGINT AUTO_INCREMENT PRIMARY KEY,
			        username VARCHAR(255) DEFAULT NULL,
			        role_name VARCHAR(255) DEFAULT NULL,
			        password VARCHAR(255) DEFAULT NULL,
			        event_type VARCHAR(100) DEFAULT NULL,
			        email VARCHAR(255) DEFAULT NULL,
			        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
			    );
			""";
		statement.executeUpdate(createFailedKeycloakEventsTableSQL);
	    } catch (SQLException e) {
		System.err.println("❌ Database or table creation failed: " + e.getMessage());
	    }
	};
    }
}