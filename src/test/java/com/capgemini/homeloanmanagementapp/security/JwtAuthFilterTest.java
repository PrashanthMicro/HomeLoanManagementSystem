package com.capgemini.homeloanmanagementapp.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthFilterTest {

    private JwtService jwtService;
    private CustomUserDetailsService userDetailsService;
    private JwtAuthFilter filter;

    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain chain;

    @BeforeEach
    void setup() {
        jwtService = mock(JwtService.class);
        userDetailsService = mock(CustomUserDetailsService.class);
        filter = new JwtAuthFilter(jwtService, userDetailsService);

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        chain = mock(FilterChain.class);

        // Clean context before every test
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    // ------------------------------------------------------------
    // 1. No Authorization header
    // ------------------------------------------------------------
    @Test
    void doFilter_noAuthHeader() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getMethod()).thenReturn("GET");

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    // ------------------------------------------------------------
    // 2. Header without Bearer prefix
    // ------------------------------------------------------------
    @Test
    void doFilter_invalidHeader_noBearerPrefix() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("XYZ");
        when(request.getRequestURI()).thenReturn("/api/x");
        when(request.getMethod()).thenReturn("POST");

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    // ------------------------------------------------------------
    // 3. Bearer token but jwtService.isValid() = false
    // ------------------------------------------------------------
    @Test
    void doFilter_invalidToken() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer abc123");
        when(request.getRequestURI()).thenReturn("/api/invalid");
        when(request.getMethod()).thenReturn("GET");

        when(jwtService.isValid("abc123")).thenReturn(false);

        filter.doFilterInternal(request, response, chain);

        verify(jwtService).isValid("abc123");
        verify(chain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    // ------------------------------------------------------------
    // 4. jwtService throws exception
    // ------------------------------------------------------------
    @Test
    void doFilter_jwtServiceThrowsException() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer tokenErr");
        when(request.getRequestURI()).thenReturn("/api/err");
        when(request.getMethod()).thenReturn("PUT");

        when(jwtService.isValid("tokenErr")).thenThrow(new RuntimeException("boom"));

        filter.doFilterInternal(request, response, chain);

        verify(jwtService).isValid("tokenErr");
        verify(chain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    // ------------------------------------------------------------
    // 5. Valid token but SecurityContext already authenticated → skip
    // ------------------------------------------------------------
    @Test
    void doFilter_validToken_contextAlreadyAuthenticated() throws Exception {

        // Prepare a mock authenticated context
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("existing", null, List.of())
        );

        when(request.getHeader("Authorization")).thenReturn("Bearer goodtoken");
        when(request.getRequestURI()).thenReturn("/api/check");
        when(request.getMethod()).thenReturn("GET");

        when(jwtService.isValid("goodtoken")).thenReturn(true);
        when(jwtService.extractUsername("goodtoken")).thenReturn("alice");

        filter.doFilterInternal(request, response, chain);

        // Verify extraction still happened
        verify(jwtService).isValid("goodtoken");

        // Authentication should NOT be overwritten
        assertEquals("existing", SecurityContextHolder.getContext().getAuthentication().getPrincipal());

        verify(chain).doFilter(request, response);
    }

    // ------------------------------------------------------------
    // 6. Valid token → authenticate user normally
    // ------------------------------------------------------------
    @Test
    void doFilter_validToken_authenticatesUser() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer goodtoken");
        when(request.getRequestURI()).thenReturn("/api/secure");
        when(request.getMethod()).thenReturn("GET");

        when(jwtService.isValid("goodtoken")).thenReturn(true);
        when(jwtService.extractUsername("goodtoken")).thenReturn("alice");

        UserDetails ud = User.withUsername("alice")
                .password("pass")
                .authorities("ROLE_USER")
                .build();

        when(userDetailsService.loadUserByUsername("alice")).thenReturn(ud);

        filter.doFilterInternal(request, response, chain);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals(ud, auth.getPrincipal());
        assertTrue(auth.isAuthenticated());
        assertEquals(1, auth.getAuthorities().size());

        verify(jwtService).isValid("goodtoken");
        verify(jwtService).extractUsername("goodtoken");
        verify(userDetailsService).loadUserByUsername("alice");
        verify(chain).doFilter(request, response);
    }
}