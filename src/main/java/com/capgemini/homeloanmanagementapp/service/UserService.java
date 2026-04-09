package com.capgemini.homeloanmanagementapp.service;

import com.capgemini.homeloanmanagementapp.model.Role;
import com.capgemini.homeloanmanagementapp.model.User;
import com.capgemini.homeloanmanagementapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;   // <-- added
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Slf4j   // <-- added
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepo;
    private final PasswordEncoder encoder;

    public User registerCustomer(String username, String rawPassword, String fullName, String email) {
        long t0 = System.currentTimeMillis();
        log.info("Registering CUSTOMER user: username={}", username);

        User u = User.builder()
                .username(username)
                .password(encoder.encode(rawPassword))
                .fullName(fullName)
                .email(email)
                .roles(Set.of(Role.ROLE_USER))
                .build();

        User saved = userRepo.save(u);
        log.info("Customer registered successfully: username={}, id={} ({} ms)",
                username, saved.getId(), System.currentTimeMillis() - t0);

        return saved;
    }

    public User registerAdmin(String username, String rawPassword, String fullName, String email) {
        long t0 = System.currentTimeMillis();
        log.info("Registering ADMIN user: username={}", username);

        User u = User.builder()
                .username(username)
                .password(encoder.encode(rawPassword))
                .fullName(fullName)
                .email(email)
                .roles(Set.of(Role.ROLE_ADMIN))
                .build();

        User saved = userRepo.save(u);
        log.info("Admin registered successfully: username={}, id={} ({} ms)",
                username, saved.getId(), System.currentTimeMillis() - t0);

        return saved;
    }

    public User findByUsername(String username) {
        log.debug("Looking up user by username={}", username);
        return userRepo.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("User not found: username={}", username);
                    return new RuntimeException("User not found");
                });
    }
}