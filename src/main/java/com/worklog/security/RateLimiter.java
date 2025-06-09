package com.worklog.security;

import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimiter {
    private final Map<String, RequestCount> requestCounts = new ConcurrentHashMap<>();
    private static final int MAX_REQUESTS = 5;
    private static final long WINDOW_SIZE_MS = 60_000; // 1 minute

    private static class RequestCount {
        final long timestamp;
        final AtomicInteger count;

        RequestCount(long timestamp) {
            this.timestamp = timestamp;
            this.count = new AtomicInteger(1);
        }
    }

    public boolean tryConsume(String key) {
        long now = System.currentTimeMillis();
        RequestCount count = requestCounts.compute(key, (k, v) -> {
            if (v == null || now - v.timestamp > WINDOW_SIZE_MS) {
                return new RequestCount(now);
            }
            v.count.incrementAndGet();
            return v;
        });
        return count.count.get() <= MAX_REQUESTS;
    }
} 