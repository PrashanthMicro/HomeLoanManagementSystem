package com.capgemini.homeloanmanagementapp.controller;

import com.capgemini.homeloanmanagementapp.dto.*;
import com.capgemini.homeloanmanagementapp.model.User;
import com.capgemini.homeloanmanagementapp.security.JwtService;
import com.capgemini.homeloanmanagementapp.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // <-- added
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

@Slf4j // <-- added
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        long t0 = System.currentTimeMillis();
        log.info("Register request received for username={}", req.getUsername());
        try {
            User u = userService.registerCustomer(req.getUsername(), req.getPassword(), req.getFullName(), req.getEmail());
            log.debug("User persisted with username={} and id={}", u.getUsername(), u.getId());

            Authentication auth = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));

            String[] roles = auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .toArray(String[]::new);

            log.info("Registration authentication success for username={}, rolesCount={}", req.getUsername(), roles.length);

            String token = jwtService.generate(req.getUsername(), roles);
            log.debug("JWT generated for username={} (length={})", req.getUsername(), token != null ? token.length() : 0);

            var resp = AuthResponse.builder().token(token).username(u.getUsername()).roles(roles).build();
            log.info("Register response ready for username={} in {} ms", req.getUsername(), (System.currentTimeMillis() - t0));
            return ResponseEntity.ok(resp);
        } catch (BadCredentialsException e) {
            log.warn("Registration authentication failed for username={} (bad credentials)", req.getUsername());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during register for username={}: {}", req.getUsername(), e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        long t0 = System.currentTimeMillis();
        log.info("Login attempt for username={}", req.getUsername());
        try {
            Authentication auth = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));

            String[] roles = auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .toArray(String[]::new);

            log.info("Login success for username={}, rolesCount={}", req.getUsername(), roles.length);

            String token = jwtService.generate(req.getUsername(), roles);
            log.debug("JWT generated for username={} (length={})", req.getUsername(), token != null ? token.length() : 0);

            var resp = AuthResponse.builder().token(token).username(req.getUsername()).roles(roles).build();
            log.info("Login response ready for username={} in {} ms", req.getUsername(), (System.currentTimeMillis() - t0));
            return ResponseEntity.ok(resp);
        } catch (BadCredentialsException e) {
            log.warn("Login failed for username={} (bad credentials)", req.getUsername());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during login for username={}: {}", req.getUsername(), e.getMessage(), e);
            throw e;
        }
    }
}