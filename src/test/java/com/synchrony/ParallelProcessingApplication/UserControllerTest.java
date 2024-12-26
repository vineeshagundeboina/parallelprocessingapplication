package com.synchrony.ParallelProcessingApplication;

import com.synchrony.ParallelProcessingApplication.controller.UserController;
import com.synchrony.ParallelProcessingApplication.model.UserEntity;
import com.synchrony.ParallelProcessingApplication.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.*;

class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    public UserControllerTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSaveUser_Success() {
        // Arrange
        UserEntity user = new UserEntity();
        user.setName("John Doe");
        user.setEmail("john@example.com");

        when(userService.saveUser(user)).thenReturn(user);

        // Act
        ResponseEntity<UserEntity> response = userController.saveUser(user);

        // Assert
        assertNotNull(response);
        assertEquals(OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("John Doe", response.getBody().getName());
        verify(userService, times(1)).saveUser(user);
    }

    @Test
    void testSaveUser_InternalServerError() {
        // Arrange
        UserEntity user = new UserEntity();
        user.setName("John Doe");
        user.setEmail("john@example.com");

        when(userService.saveUser(user)).thenThrow(new RuntimeException("Database error"));

        // Act
        ResponseEntity<UserEntity> response = userController.saveUser(user);

        // Assert
        assertNotNull(response);
        assertEquals(INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
        verify(userService, times(1)).saveUser(user);
    }

    @Test
    void testGetUserById_Success() {
        // Arrange
        Long userId = 1L;
        UserEntity user = new UserEntity();
        user.setId(userId);
        user.setName("Jane Doe");

        when(userService.getUserById(userId)).thenReturn(user);

        // Act
        ResponseEntity<UserEntity> response = userController.getUserById(userId);

        // Assert
        assertNotNull(response);
        assertEquals(OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Jane Doe", response.getBody().getName());
        verify(userService, times(1)).getUserById(userId);
    }

    @Test
    void testGetUserById_NotFound() {
        // Arrange
        Long userId = 1L;

        when(userService.getUserById(userId)).thenReturn(null);

        // Act
        ResponseEntity<UserEntity> response = userController.getUserById(userId);

        // Assert
        assertNotNull(response);
        assertEquals(NOT_FOUND, response.getStatusCode());
        verify(userService, times(1)).getUserById(userId);
    }

    @Test
    void testGetAllUsers_Success() {
        // Arrange
        UserEntity user1 = new UserEntity();
        user1.setName("John Doe");
        UserEntity user2 = new UserEntity();
        user2.setName("Jane Doe");

        List<UserEntity> users = Arrays.asList(user1, user2);

        when(userService.getAllUsers()).thenReturn(users);

        // Act
        ResponseEntity<List<UserEntity>> response = userController.getAllUsers();

        // Assert
        assertNotNull(response);
        assertEquals(OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(userService, times(1)).getAllUsers();
    }

    @Test
    void testGetAllUsers_InternalServerError() {
        // Arrange
        when(userService.getAllUsers()).thenThrow(new RuntimeException("Database error"));

        // Act
        ResponseEntity<List<UserEntity>> response = userController.getAllUsers();

        // Assert
        assertNotNull(response);
        assertEquals(INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
        verify(userService, times(1)).getAllUsers();
    }

    @Test
    void testUpdateUser_Success() {
        // Arrange
        Long userId = 1L;
        UserEntity user = new UserEntity();
        user.setName("Updated Name");
        user.setEmail("updated@example.com");

        when(userService.updateUser(eq(userId), any(UserEntity.class))).thenReturn(user);

        // Act
        ResponseEntity<UserEntity> response = userController.updateUser(userId, user);

        // Assert
        assertNotNull(response);
        assertEquals(OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Updated Name", response.getBody().getName());
        verify(userService, times(1)).updateUser(eq(userId), any(UserEntity.class));
    }

    @Test
    void testUpdateUser_NotFound() {
        // Arrange
        Long userId = 1L;
        UserEntity user = new UserEntity();
        user.setName("Updated Name");

        when(userService.updateUser(eq(userId), any(UserEntity.class))).thenThrow(new RuntimeException("User not found"));

        // Act
        ResponseEntity<UserEntity> response = userController.updateUser(userId, user);

        // Assert
        assertNotNull(response);
        assertEquals(NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(userService, times(1)).updateUser(eq(userId), any(UserEntity.class));
    }

    @Test
    void testDeleteUser_Success() {
        // Arrange
        Long userId = 1L;
        doNothing().when(userService).deleteUser(userId);

        // Act
        ResponseEntity<Void> response = userController.deleteUser(userId);

        // Assert
        assertNotNull(response);
        assertEquals(NO_CONTENT, response.getStatusCode());
        verify(userService, times(1)).deleteUser(userId);
    }

    @Test
    void testDeleteUser_NotFound() {
        // Arrange
        Long userId = 1L;
        doThrow(new RuntimeException("User not found")).when(userService).deleteUser(userId);

        // Act
        ResponseEntity<Void> response = userController.deleteUser(userId);

        // Assert
        assertNotNull(response);
        assertEquals(NOT_FOUND, response.getStatusCode());
        verify(userService, times(1)).deleteUser(userId);
    }
}
