package com.threadstack.user.config;

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

	@Value("${spring.datasource.url}")
	private String jdbcUrl;

	@Value("${spring.r2dbc.username}")
	private String dbUser;

	@Value("${spring.r2dbc.password}")
	private String dbPassword;

	private static final String DATABASE_NAME = "threadstack";

	@Bean
	public CommandLineRunner initializeDatabase() {
		return args -> {
			String jdbcBaseUrl = jdbcUrl.replace("r2dbc:mysql://", "jdbc:mysql://").replace("/" + DATABASE_NAME, "");

			try (Connection connection = DriverManager.getConnection(jdbcBaseUrl, dbUser, dbPassword);
					Statement statement = connection.createStatement()) {

				// Create database if it doesn't exist
				statement.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DATABASE_NAME);
				System.out.println("✅ Database '" + DATABASE_NAME + "' checked/created successfully!");

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
				System.out.println("✅ Table 'users' checked/created successfully!");

			} catch (SQLException e) {
				System.err.println("❌ Database or table creation failed: " + e.getMessage());
			}
		};
	}
}