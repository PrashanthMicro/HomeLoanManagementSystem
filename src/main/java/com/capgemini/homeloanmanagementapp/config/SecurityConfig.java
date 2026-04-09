package com.capgemini.homeloanmanagementapp.config;

import com.capgemini.homeloanmanagementapp.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;                 // <-- added
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Slf4j                                                // <-- added
@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        log.info("Initializing SecurityFilterChain (stateless JWT, custom rules)"); // <-- added

        http
                // 1) CSRF off for stateless APIs
                .csrf(csrf -> {
                    log.debug("Disabling CSRF (stateless JWT API)");                    // <-- added
                    csrf.disable();
                })

                // 2) Stateless sessions
                .sessionManagement(sm -> {
                    log.debug("Setting SessionCreationPolicy=STATELESS");               // <-- added
                    sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
                })

                // 3) Authorization rules
                .authorizeHttpRequests(auth -> {
                    log.debug("Configuring URL authorization rules");                   // <-- added
                    auth.requestMatchers("/api/auth/**").permitAll();
                    auth.requestMatchers("/api/products/**").permitAll();
                    auth.requestMatchers("/api/loans/admin/**").hasRole("ADMIN");
                    auth.requestMatchers("/api/loans/**").authenticated();
                    auth.anyRequest().authenticated();
                })

                // 4) Provider + JWT filter
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        SecurityFilterChain chain = http.build();
        log.info("SecurityFilterChain built successfully");                          // <-- added
        return chain;
    }

    // Password encoder bean (used by DaoAuthenticationProvider & UserService)
    @Bean
    public PasswordEncoder passwordEncoder() {
        log.debug("Creating BCryptPasswordEncoder bean");                             // <-- added
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        log.debug("Creating DaoAuthenticationProvider bean");                         // <-- added
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder());
        provider.setUserDetailsService(userDetailsService);
        log.info("DaoAuthenticationProvider configured with UserDetailsService={} and BCryptPasswordEncoder",
                userDetailsService.getClass().getSimpleName());                       // <-- added
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration config)
            throws Exception {
        log.debug("Exposing AuthenticationManager from AuthenticationConfiguration");  // <-- added
        return config.getAuthenticationManager();
    }
}