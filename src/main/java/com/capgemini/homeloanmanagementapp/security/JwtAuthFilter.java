package com.capgemini.homeloanmanagementapp.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // <-- added
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j // <-- added
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        final String uri = request.getRequestURI();
        final String method = request.getMethod();
        final String authHeader = request.getHeader("Authorization");

        String username = null;
        String token = null;

        // Light request trace (without sensitive data)
        log.debug("JwtAuthFilter START {} {}", method, uri);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            try {
                if (jwtService.isValid(token)) {
                    username = jwtService.extractUsername(token);
                    log.debug("JWT valid for user={}", username);
                } else {
                    log.warn("Invalid JWT received for {} {}", method, uri);
                }
            } catch (Exception e) {
                // Do not fail the request here; let the security chain decide (entry point -> 401)
                log.warn("JWT parsing/validation failed: {}", e.getMessage());
            }
        } else {
            log.debug("No Bearer token in Authorization header for {} {}", method, uri);
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // Load authorities from DB
            UserDetails ud = userDetailsService.loadUserByUsername(username);
            var auth = new UsernamePasswordAuthenticationToken(ud, null, ud.getAuthorities());
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(auth);
            log.info("SecurityContext authenticated user={} with authorities={}",
                    ud.getUsername(),
                    ud.getAuthorities()
                            .stream()
                            .map(a -> a.getAuthority())
                            .toList());
        }

        chain.doFilter(request, response);
        log.debug("JwtAuthFilter END {} {} -> status={}", method, uri, response.getStatus());
    }
}