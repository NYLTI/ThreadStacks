package com.threadstack.room.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Document(collection = "rooms")
public class Room {
    @Id
    private String id;

    @NotBlank(message = "Room name cannot be blank")
    @Size(min = 3, max = 50, message = "Room name must be between 3 and 50 characters")
    @Indexed(unique = true)
    private String name;

    @Size(max = 255, message = "Description cannot exceed 255 characters")
    private String description;

    private final LocalDateTime createdAt = LocalDateTime.now();

    private String createdBy = "Admin";

    private RoomType roomType = RoomType.PUBLIC;

    private Set<String> members = new HashSet<>();
    
    private Set<String> moderators = new HashSet<>();
}
