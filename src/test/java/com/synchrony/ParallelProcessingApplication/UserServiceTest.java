



package com.synchrony.ParallelProcessingApplication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.synchrony.ParallelProcessingApplication.model.UserEntity;
import com.synchrony.ParallelProcessingApplication.repository.UserRepository;
import com.synchrony.ParallelProcessingApplication.service.UserService;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private Logger logger;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    public void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    public static final String CACHE_KEY_PREFIX = "USER_";
    
    public static final String CACHE_KEY_ALL_USERS = "ALL_USERS";


    @Test
    public void testSaveUser_Success() {
        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setName("John Doe");
        user.setEmail("john.doe@example.com");

        when(userRepository.save(any(UserEntity.class))).thenReturn(user);

        UserEntity result = userService.saveUser(user);

        assertNotNull(result);
        assertEquals("John Doe", result.getName());
        verify(userRepository, times(1)).save(user);
        verify(valueOperations, times(1)).set("USER_1", user);
        verify(redisTemplate, times(1)).delete("ALL_USERS");
    }

    @Test
    public void testGetUserById_FoundInCache() {
        UserEntity cachedUser = new UserEntity();
        cachedUser.setId(1L);
        cachedUser.setName("Cached User");

        when(valueOperations.get("USER_1")).thenReturn(cachedUser);

        UserEntity result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals("Cached User", result.getName());
        verify(userRepository, never()).findById(1L);
    }

    @Test
    public void testGetUserById_NotFoundInCacheButFoundInDB() {
        UserEntity dbUser = new UserEntity();
        dbUser.setId(1L);
        dbUser.setName("Database User");

        when(valueOperations.get("USER_1")).thenReturn(null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(dbUser));

        UserEntity result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals("Database User", result.getName());
        verify(userRepository, times(1)).findById(1L);
        verify(valueOperations, times(1)).set("USER_1", dbUser);
    }

    @Test
    public void testGetUserById_NotFoundAnywhere() {
        when(valueOperations.get("USER_1")).thenReturn(null);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        UserEntity result = userService.getUserById(1L);

        assertNull(result);
    }

    @Test
    public void testGetAllUsers_FoundInCache() {
        List<UserEntity> cachedUsers = new ArrayList<>();
        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setName("Cached User");
        cachedUsers.add(user);

        when(valueOperations.get("ALL_USERS")).thenReturn(cachedUsers);

        List<UserEntity> result = userService.getAllUsers();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userRepository, never()).findAll();
    }

    @Test
    public void testGetAllUsers_NotFoundInCacheButFoundInDB() {
        List<UserEntity> dbUsers = new ArrayList<>();
        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setName("Database User");
        dbUsers.add(user);

        when(valueOperations.get("ALL_USERS")).thenReturn(null);
        when(userRepository.findAll()).thenReturn(dbUsers);

        List<UserEntity> result = userService.getAllUsers();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userRepository, times(1)).findAll();
        verify(valueOperations, times(1)).set("ALL_USERS", dbUsers);
    }
//    @Test
//    public void testUpdateUser_Success() {
//        Long userId = 1L;
//
//        // Existing user in DB
//        UserEntity existingUser = new UserEntity();
//        existingUser.setId(userId);
//        existingUser.setName("Old Name");
//        existingUser.setEmail("old@example.com");
//
//        // Updated user details
//        UserEntity updatedUser = new UserEntity();
//        updatedUser.setName("New Name");
//        updatedUser.setEmail("new@example.com");
//
//        // Mock behavior
//        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
//        when(userRepository.save(Mockito.any(UserEntity.class))).thenReturn(updatedUser);
//        doNothing().when(redisTemplate).delete("ALL_USERS");
//
//        // Call the method
//        UserEntity result = userService.updateUser(userId, updatedUser);
//
//        // Assertions
//        assertNotNull(result);
//        assertEquals("New Name", result.getName());
//        assertEquals("new@example.com", result.getEmail());
//
//        // Verify interactions
//        verify(userRepository, times(1)).findById(userId); // Verify user lookup
//        verify(userRepository, times(1)).save(Mockito.any(UserEntity.class)); // Verify save operation
//        verify(redisTemplate, times(1)).opsForValue().set("USER_" + userId, updatedUser); // Verify cache update
//        verify(redisTemplate, times(1)).delete("ALL_USERS"); // Verify all-users cache invalidation
//    }

//    @Test
//    public void testDeleteUser_Success() {
//        // Arrange
//        Long userId = 1L;
//
//        // Mock behavior for repository and Redis
//        doNothing().when(userRepository).deleteById(userId); // Mock repository deletion (void method)
//        when(redisTemplate.delete(userService.CACHE_KEY_PREFIX + userId)).thenReturn(true); // Mock user-specific cache deletion (non-void method)
//        when(redisTemplate.delete(userService.CACHE_KEY_ALL_USERS)).thenReturn(true); // Mock all-users cache invalidation (non-void method)
//
//        // Act
//        userService.deleteUser(userId);
//
//        // Assert
//        // Verify that the repository method is called exactly once
//        verify(userRepository, times(1)).deleteById(userId);
//        // Verify that the Redis template's delete method is called with the correct key for the user
//        verify(redisTemplate, times(1)).delete(userService.CACHE_KEY_PREFIX + userId);
//        // Verify that the Redis template's delete method is called with the key for all users
//        verify(redisTemplate, times(1)).delete(userService.CACHE_KEY_ALL_USERS);
//    }
}
