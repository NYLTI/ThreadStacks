package com.threadstack.user.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

@Table("users")
@Data
@AllArgsConstructor
@Accessors(chain = true)
public class User {
	@Id
    private Long id;

    @NotBlank(message = "Username cannot be blank")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Column("username")
    private String username;
    
    @NotBlank(message = "First name cannot be blank")
    @Size(min = 2, max = 50, message = "Please enter a valid name")
    @Column("first_name")
    private String firstName;
    
    @NotBlank(message = "Last name cannot be blank")
    @Size(min = 2, max = 50, message = "Please enter a valid name")
    private String lastName;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email must be valid")
    @Column("email")
    private String email;

    @NotBlank(message = "Password cannot be blank")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    private String password;

    @Column("role")
    private Role role;

    @Column("created_at")
    private LocalDateTime createdAt;
    
}
