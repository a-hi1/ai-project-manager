package com.example.backend.service;

import com.example.backend.entity.User;
import com.example.backend.mapper.UserMapper;
import com.example.backend.utils.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1);
        testUser.setUsername("testuser");
        testUser.setPassword("password123");
        testUser.setEmail("test@example.com");
    }

    @Test
    void testLogin_Success() {
        when(userMapper.findByUsername("testuser")).thenReturn(testUser);
        when(jwtUtils.generateToken("testuser")).thenReturn("mock-jwt-token");

        String token = userService.login("testuser", "password123");

        assertNotNull(token);
        assertEquals("mock-jwt-token", token);
        verify(userMapper).findByUsername("testuser");
        verify(jwtUtils).generateToken("testuser");
    }

    @Test
    void testLogin_UserNotFound() {
        when(userMapper.findByUsername("nonexistent")).thenReturn(null);

        String token = userService.login("nonexistent", "password");

        assertNull(token);
        verify(userMapper).findByUsername("nonexistent");
        verify(jwtUtils, never()).generateToken(anyString());
    }

    @Test
    void testLogin_WrongPassword() {
        when(userMapper.findByUsername("testuser")).thenReturn(testUser);

        String token = userService.login("testuser", "wrongpassword");

        assertNull(token);
        verify(userMapper).findByUsername("testuser");
        verify(jwtUtils, never()).generateToken(anyString());
    }

    @Test
    void testFindByUsername() {
        when(userMapper.findByUsername("testuser")).thenReturn(testUser);

        User found = userService.findByUsername("testuser");

        assertNotNull(found);
        assertEquals("testuser", found.getUsername());
        assertEquals("test@example.com", found.getEmail());
    }

    @Test
    void testSelectById() {
        when(userMapper.selectById(1)).thenReturn(testUser);

        User found = userService.selectById(1);

        assertNotNull(found);
        assertEquals(1, found.getId());
    }
}
