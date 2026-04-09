package com.capgemini.homeloanmanagementapp.service;

import com.capgemini.homeloanmanagementapp.model.Role;
import com.capgemini.homeloanmanagementapp.model.User;
import com.capgemini.homeloanmanagementapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserRepository userRepo;
    private PasswordEncoder encoder;
    private UserService service;

    @BeforeEach
    void setup() {
        userRepo = mock(UserRepository.class);
        encoder = mock(PasswordEncoder.class);
        service = new UserService(userRepo, encoder);
    }

    // --------------------------------------------------------------------
    // registerCustomer
    // --------------------------------------------------------------------
    @Test
    void registerCustomer_encodesPassword_setsRoleUser_andSaves() {
        String username = "alice";
        String raw = "plain-pass";
        String encoded = "enc-pass";
        String fullName = "Alice A";
        String email = "alice@example.com";

        when(encoder.encode(raw)).thenReturn(encoded);

        // Return a "saved" user with an id
        when(userRepo.save(any(User.class))).thenAnswer(inv -> {
            User in = inv.getArgument(0);
            User saved = new User();
            saved.setId(100L);
            saved.setUsername(in.getUsername());
            saved.setPassword(in.getPassword());
            saved.setFullName(in.getFullName());
            saved.setEmail(in.getEmail());
            saved.setRoles(in.getRoles());
            return saved;
        });

        User saved = service.registerCustomer(username, raw, fullName, email);

        // Verify encoder usage
        verify(encoder, times(1)).encode(raw);

        // Capture the entity passed to repo.save
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepo, times(1)).save(captor.capture());
        User persisted = captor.getValue();

        // Assertions on persisted values
        assertEquals(username, persisted.getUsername());
        assertEquals(fullName, persisted.getFullName());
        assertEquals(email, persisted.getEmail());
        assertEquals(Set.of(Role.ROLE_USER), persisted.getRoles());
        assertEquals(encoded, persisted.getPassword()); // encoded password

        // Returned object assertions
        assertEquals(100L, saved.getId());
        assertEquals(Set.of(Role.ROLE_USER), saved.getRoles());
        assertEquals(encoded, saved.getPassword());
    }

    // --------------------------------------------------------------------
    // registerAdmin
    // --------------------------------------------------------------------
    @Test
    void registerAdmin_encodesPassword_setsRoleAdmin_andSaves() {
        String username = "admin";
        String raw = "admin-pass";
        String encoded = "enc-admin-pass";
        String fullName = "Admin A";
        String email = "admin@example.com";

        when(encoder.encode(raw)).thenReturn(encoded);

        when(userRepo.save(any(User.class))).thenAnswer(inv -> {
            User in = inv.getArgument(0);
            User saved = new User();
            saved.setId(200L);
            saved.setUsername(in.getUsername());
            saved.setPassword(in.getPassword());
            saved.setFullName(in.getFullName());
            saved.setEmail(in.getEmail());
            saved.setRoles(in.getRoles());
            return saved;
        });

        User saved = service.registerAdmin(username, raw, fullName, email);

        verify(encoder, times(1)).encode(raw);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepo, times(1)).save(captor.capture());
        User persisted = captor.getValue();

        assertEquals(username, persisted.getUsername());
        assertEquals(fullName, persisted.getFullName());
        assertEquals(email, persisted.getEmail());
        assertEquals(Set.of(Role.ROLE_ADMIN), persisted.getRoles());
        assertEquals(encoded, persisted.getPassword());

        assertEquals(200L, saved.getId());
        assertEquals(Set.of(Role.ROLE_ADMIN), saved.getRoles());
        assertEquals(encoded, saved.getPassword());
    }

    // --------------------------------------------------------------------
    // findByUsername
    // --------------------------------------------------------------------
    @Test
    void findByUsername_whenFound_returnsUser() {
        User u = new User();
        u.setId(10L);
        u.setUsername("bob");
        u.setPassword("x");

        when(userRepo.findByUsername("bob")).thenReturn(Optional.of(u));

        User result = service.findByUsername("bob");

        assertSame(u, result);
        verify(userRepo, times(1)).findByUsername("bob");
    }

    @Test
    void findByUsername_whenMissing_throwsRuntimeException() {
        when(userRepo.findByUsername("missing")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.findByUsername("missing"));

        assertEquals("User not found", ex.getMessage());
        verify(userRepo, times(1)).findByUsername("missing");
    }
}