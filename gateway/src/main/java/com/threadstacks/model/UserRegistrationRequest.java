package com.threadstacks.model;

import lombok.Data;

@Data
public class UserRegistrationRequest {
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
}