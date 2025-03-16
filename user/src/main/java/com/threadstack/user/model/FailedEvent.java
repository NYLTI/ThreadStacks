package com.threadstack.user.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("failed_events")
public class FailedEvent {

    @Id
    private Long id;
    private String topic;
    private String eventData;
    private LocalDateTime createdAt = LocalDateTime.now();

    public FailedEvent(String topic, String eventData) {
        this.topic = topic;
        this.eventData = eventData;
        this.createdAt = LocalDateTime.now();
    }
}
