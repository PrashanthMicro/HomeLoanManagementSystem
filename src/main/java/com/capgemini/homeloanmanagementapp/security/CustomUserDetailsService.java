package com.capgemini.homeloanmanagementapp.security;

import com.capgemini.homeloanmanagementapp.model.User;
import com.capgemini.homeloanmanagementapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // <-- added
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Slf4j // <-- added
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Loading UserDetails for username={}", username); // minimal PII-safe log
        User u = userRepo.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("User not found for username={}", username);
                    return new UsernameNotFoundException("User not found");
                });

        var authorities = u.getRoles().stream()
                .map(r -> new SimpleGrantedAuthority(r.name()))
                .collect(Collectors.toList());

        log.debug("Resolved authorities for username={}: {}", username,
                authorities.stream().map(SimpleGrantedAuthority::getAuthority).toList());

        // Do not log the password hash for security hygiene
        return new org.springframework.security.core.userdetails.User(
                u.getUsername(),
                u.getPassword(),
                authorities
        );
    }
}