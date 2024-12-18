package io.hhplus.tdd.point.service;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class LockManager {
    private final ConcurrentHashMap<Long, ReentrantLock> lockMap = new ConcurrentHashMap<>();

    public ReentrantLock getLock(Long id) {
        return lockMap.computeIfAbsent(id, k -> new ReentrantLock(true));
    }
}
