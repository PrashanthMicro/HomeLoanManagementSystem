package com.capgemini.homeloanmanagementapp.repository;

import com.capgemini.homeloanmanagementapp.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager em;

    private User persistUser(String username) {
        User u = new User();
        u.setUsername(username);
        u.setPassword("encoded-pass");
        u.setEmail(username + "@example.com");
        u.setFullName("Full " + username);
        return em.persistAndFlush(u);
    }

    @Test
    void findByUsername_whenUserExists_returnsUser() {
        User saved = persistUser("alice");

        Optional<User> found = userRepository.findByUsername("alice");

        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
        assertEquals("alice", found.get().getUsername());
        assertEquals("encoded-pass", found.get().getPassword());
        assertEquals("alice@example.com", found.get().getEmail());
        assertEquals("Full alice", found.get().getFullName());
    }

    @Test
    void findByUsername_whenUserMissing_returnsEmptyOptional() {
        // No users persisted with username "missing"
        Optional<User> found = userRepository.findByUsername("missing");
        assertTrue(found.isEmpty());
    }

    @Test
    void findByUsername_withMultipleUsers_returnsCorrectOne() {
        persistUser("bob");
        persistUser("carol");

        Optional<User> bob = userRepository.findByUsername("bob");
        Optional<User> carol = userRepository.findByUsername("carol");

        assertTrue(bob.isPresent());
        assertEquals("bob", bob.get().getUsername());

        assertTrue(carol.isPresent());
        assertEquals("carol", carol.get().getUsername());
    }
}