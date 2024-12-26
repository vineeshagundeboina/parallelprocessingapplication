


package com.synchrony.ParallelProcessingApplication.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.synchrony.ParallelProcessingApplication.model.UserEntity;
import com.synchrony.ParallelProcessingApplication.repository.UserRepository;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public final String CACHE_KEY_PREFIX = "USER_";
    public final String CACHE_KEY_ALL_USERS = "ALL_USERS";

    // Create or update a user
    @Transactional
    public UserEntity saveUser(UserEntity user) {
        try {
            logger.info("Saving user: {}", user);
            UserEntity savedUser = userRepository.save(user);

            // Cache the saved user
            redisTemplate.opsForValue().set(CACHE_KEY_PREFIX + savedUser.getId(), savedUser);
            invalidateAllUsersCache();
            return savedUser;
        } catch (Exception e) {
            logger.error("Error while saving user: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save user");
        }
    }

    // Get a user by ID
    public UserEntity getUserById(Long id) {
        String cacheKey = CACHE_KEY_PREFIX + id;
        try {
            // Check Redis cache first
            UserEntity cachedUser = (UserEntity) redisTemplate.opsForValue().get(cacheKey);
            if (cachedUser != null) {
                logger.info("User found in cache for ID: {}", id);
                return cachedUser;
            }

            // Fetch from DB if not cached
            logger.info("Fetching user from database for ID: {}", id);
            Optional<UserEntity> user = userRepository.findById(id);
            user.ifPresent(value -> redisTemplate.opsForValue().set(cacheKey, value)); // Cache the result
            return user.orElse(null);
        } catch (Exception e) {
            logger.error("Error while fetching user by ID: {}", id, e);
            throw new RuntimeException("Failed to fetch user");
        }
    }

    // Get all users
    public List<UserEntity> getAllUsers() {
        try {
            logger.info("Fetching all users");
            // Check Redis cache
            List<UserEntity> cachedUsers = (List<UserEntity>) redisTemplate.opsForValue().get(CACHE_KEY_ALL_USERS);
            if (cachedUsers != null) {
                logger.info("All users found in cache");
                return cachedUsers;
            }

            // Fetch from DB if not cached
            logger.info("Fetching before all users from database"+LocalDate.now());
            List<UserEntity> users = userRepository.findAll();
            redisTemplate.opsForValue().set(CACHE_KEY_ALL_USERS, users); // Cache the result
            logger.info("Fetching after all users from database"+LocalDate.now());
            
            return users;
        } catch (Exception e) {
            logger.error("Error while fetching all users: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch all users");
        }
    }

    // Update an existing user
    @Transactional
    public UserEntity updateUser(Long id, UserEntity user) {
        try {
            UserEntity existingUser = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            existingUser.setName(user.getName());
            existingUser.setEmail(user.getEmail());
            UserEntity updatedUser = userRepository.save(existingUser);

            // Cache updated user and invalidate all users cache
            redisTemplate.opsForValue().set(CACHE_KEY_PREFIX + updatedUser.getId(), updatedUser);
            invalidateAllUsersCache();
            return updatedUser;
        } catch (Exception e) {
            logger.error("Error while updating user: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update user");
        }
    }

    // Delete a user
    @Transactional
    public void deleteUser(Long id) {
        try {
            userRepository.deleteById(id);
            redisTemplate.delete(CACHE_KEY_PREFIX + id); // Remove user from cache
            invalidateAllUsersCache();
            logger.info("User deleted with ID: {}", id);
        } catch (Exception e) {
            logger.error("Error while deleting user: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete user");
        }
    }

    // Invalidate all users cache
    private void invalidateAllUsersCache() {
        try {
            redisTemplate.delete(CACHE_KEY_ALL_USERS);
            logger.info("All users cache invalidated");
        } catch (Exception e) {
            logger.error("Error while invalidating all users cache: {}", e.getMessage(), e);
        }
    }
}

