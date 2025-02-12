package com.threadstack.thread.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "replies")
public class Reply {

    @Id
    private String id;

    @NotBlank(message = "Comment content cannot be blank")
    @Size(max = 1000, message = "Comment cannot exceed 1000 characters")
    private String content;
    
    @NotNull(message = "Author content cannot be blank")
    private Long authorId;
    
    private String threadId;
    
    private String parentReplyId;

    private List<String> replies = new ArrayList<>();

    private final LocalDateTime createdAt = LocalDateTime.now();
}
