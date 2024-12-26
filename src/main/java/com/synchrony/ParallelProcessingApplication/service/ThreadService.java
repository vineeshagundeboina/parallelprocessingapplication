package com.synchrony.ParallelProcessingApplication.service;

import java.util.concurrent.CompletableFuture;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import com.synchrony.ParallelProcessingApplication.model.UserEntity;

@Service
@EnableAsync
public class ThreadService {
    @Async
    public CompletableFuture<Void> updateCache(UserEntity user, RedisTemplate<String, Object> redisTemplate) {
        redisTemplate.opsForValue().set("USER_" + user.getId(), user);
        return CompletableFuture.completedFuture(null);
    }
}
