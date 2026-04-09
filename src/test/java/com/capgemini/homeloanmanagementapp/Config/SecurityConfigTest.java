package com.capgemini.homeloanmanagementapp.Config;

import com.capgemini.homeloanmanagementapp.config.SecurityConfig;
import com.capgemini.homeloanmanagementapp.security.JwtAuthFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SecurityConfigTest {

    @Mock private JwtAuthFilter jwtAuthFilter;
    @Mock private UserDetailsService userDetailsService;
    @Mock private AuthenticationConfiguration authenticationConfiguration;
    @Mock private AuthenticationManager authenticationManager;

    // Important: HttpSecurity internally uses ObjectPostProcessor; make it return input
    @Mock private ObjectPostProcessor<Object> objectPostProcessor;

    @InjectMocks
    private SecurityConfig securityConfig;


    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        // Return the same object passed in; Spring Security expects a working post-processor
        when(objectPostProcessor.postProcess(any())).thenAnswer(inv -> inv.getArgument(0));

        // AuthenticationManager from AuthenticationConfiguration
        when(authenticationConfiguration.getAuthenticationManager()).thenReturn(authenticationManager);
    }

    // -----------------------------------------------------------
    // passwordEncoder() bean
    // -----------------------------------------------------------
    @Test
    void testPasswordEncoder() {
        PasswordEncoder encoder = securityConfig.passwordEncoder();
        assertNotNull(encoder);

        String raw = "secret";
        assertTrue(encoder.matches(raw, encoder.encode(raw)));
    }

    // -----------------------------------------------------------
    // authenticationProvider() bean
    // -----------------------------------------------------------
    @Test
    void testAuthenticationProvider() {
        AuthenticationProvider provider = securityConfig.authenticationProvider();
        assertNotNull(provider);
        assertTrue(provider.supports(
                org.springframework.security.authentication.UsernamePasswordAuthenticationToken.class
        ));
    }

    // -----------------------------------------------------------
    // authenticationManager() bean
    // -----------------------------------------------------------
    @Test
    void testAuthenticationManager() throws Exception {
        AuthenticationManager manager = securityConfig.authenticationManager(authenticationConfiguration);
        assertNotNull(manager);
        verify(authenticationConfiguration, times(1)).getAuthenticationManager();
    }

    // -----------------------------------------------------------
    // filterChain() bean
    // -----------------------------------------------------------
    @Test
    void testFilterChain() throws Exception {
        // Use a REAL lightweight ApplicationContext (not a mock!) to avoid NPE in authorizeHttpRequests
        GenericApplicationContext ctx = new GenericApplicationContext();
        ctx.refresh(); // prepare the context

        // Inject required shared objects (HttpSecurity looks these up)
        Map<Class<?>, Object> sharedObjects = new LinkedHashMap<>();
        sharedObjects.put(GenericApplicationContext.class, ctx); // helpful but not sufficient
        // HttpSecurity specifically asks for ApplicationContext.class, add it explicitly:
        sharedObjects.put(org.springframework.context.ApplicationContext.class, ctx);

        // Build AuthenticationManagerBuilder with a working ObjectPostProcessor
        AuthenticationManagerBuilder authBuilder = new AuthenticationManagerBuilder(objectPostProcessor);

        // Public 3-arg constructor works for Spring Security 5.x and 6.x
        HttpSecurity http = new HttpSecurity(objectPostProcessor, authBuilder, sharedObjects);

        // Execute the configuration under test
        SecurityFilterChain chain = securityConfig.filterChain(http);

        assertNotNull(chain);
        assertFalse(chain.getFilters().isEmpty(), "Filter chain should not be empty");

        // Optional: verify our jwtAuthFilter is present (addFilterBefore should have inserted it)
        assertTrue(
                chain.getFilters().stream().anyMatch(f -> f == jwtAuthFilter),
                "jwtAuthFilter should be part of the filter chain"
        );
    }
}