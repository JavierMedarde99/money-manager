package com.money.manager.infrastructure.security;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class RateLimiterService {

    private static final int MAX_BUCKETS = 10_000;
    private static final long CLEANUP_WINDOW_MILLIS = 60_000;

    private final ConcurrentHashMap<String, Window> buckets = new ConcurrentHashMap<>();

    private record Window(long windowStartMillis, int count) {
    }

    public boolean isAllowed(String key, int maxRequests, long windowMillis) {
        long now = System.currentTimeMillis();

        Window current = buckets.compute(key, (k, window) -> {
            if (window == null || now - window.windowStartMillis() >= windowMillis) {
                return new Window(now, 1);
            }
            return new Window(window.windowStartMillis(), window.count() + 1);
        });

        if (buckets.size() > MAX_BUCKETS) {
            buckets.entrySet().removeIf(entry -> now - entry.getValue().windowStartMillis() >= CLEANUP_WINDOW_MILLIS);
        }

        return current.count() <= maxRequests;
    }
}
