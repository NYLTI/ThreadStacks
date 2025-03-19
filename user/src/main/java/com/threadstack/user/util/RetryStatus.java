package com.threadstack.user.util;

import java.util.concurrent.atomic.AtomicBoolean;
import org.springframework.stereotype.Component;

@Component
public class RetryStatus {
    public static final AtomicBoolean SHOULDRETRYKAFKA = new AtomicBoolean(true);
    public static final AtomicBoolean SHOULDRETRYKEYCLOAK = new AtomicBoolean(true);
}
