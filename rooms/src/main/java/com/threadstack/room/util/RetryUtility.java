package com.threadstack.room.util;

import java.util.concurrent.atomic.AtomicBoolean;

public class RetryUtility {
    public static final AtomicBoolean SHOULDRETRYKAFKA = new AtomicBoolean(true);
}
