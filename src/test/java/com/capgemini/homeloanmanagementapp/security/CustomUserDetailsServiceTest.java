package com.capgemini.homeloanmanagementapp.security;

import com.capgemini.homeloanmanagementapp.model.Role;
import com.capgemini.homeloanmanagementapp.model.User;
import com.capgemini.homeloanmanagementapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomUserDetailsServiceTest {

    private UserRepository userRepository;
    private CustomUserDetailsService service;

    @BeforeEach
    void setup() {
        userRepository = mock(UserRepository.class);
        service = new CustomUserDetailsService(userRepository);
    }

    @Test
    void loadUserByUsername_successfullyReturnsUserDetails() {
        // Arrange
        User user = new User();
        user.setUsername("alice");
        user.setPassword("encoded-pass");
        user.setRoles(Set.of(Role.ROLE_USER, Role.ROLE_ADMIN));

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));

        // Act
        UserDetails result = service.loadUserByUsername("alice");

        // Assert
        assertNotNull(result);
        assertEquals("alice", result.getUsername());
        assertEquals("encoded-pass", result.getPassword());
        assertEquals(2, result.getAuthorities().size());
        assertTrue(result.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
        assertTrue(result.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        verify(userRepository, times(1)).findByUsername("alice");
    }

    @Test
    void loadUserByUsername_whenNotFound_throwsException() {
        // Arrange
        when(userRepository.findByUsername("bob")).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("bob"));
        verify(userRepository, times(1)).findByUsername("bob");
    }
}