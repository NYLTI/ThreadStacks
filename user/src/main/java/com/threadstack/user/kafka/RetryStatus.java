package com.threadstack.user.kafka;

import java.util.concurrent.atomic.AtomicBoolean;
import org.springframework.stereotype.Component;

@Component
public class RetryStatus {
    public static final AtomicBoolean shouldRetry = new AtomicBoolean(true);
}
