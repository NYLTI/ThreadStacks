package com.threadstack.thread.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "threads")
public class Thread {

    @Id
    private String id;

    @NotBlank(message = "Title cannot be blank")
    @Size(max = 150, message = "Title cannot exceed 150 characters")
    private String title;

    @NotBlank(message = "Thread content cannot be blank")
    @Size(max = 2000, message = "Thread content cannot exceed 2000 characters")
    private String content;
    
    private String userName;
    
    @NotBlank(message = "Room name cannot be blank")
    private String roomName;
    
    private final LocalDateTime createdAt = LocalDateTime.now();
    
    private LocalDateTime lastUpdated;

    private Set<String> tags = new HashSet<>();

    private boolean pinned = false;

    private ArrayList<String> replies = new ArrayList<>();
}
