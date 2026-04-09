//package com.capgemini.homeloanmanagementapp.security;
//
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//
//@Configuration
//@EnableWebSecurity
//@RequiredArgsConstructor
//public class SecurityConfig {
//
//    private final JwtAuthFilter jwtAuthFilter;
//
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//
//        http
//                // 🚀 CRITICAL: Disable CSRF for stateless JWT based APIs
//                .csrf(csrf -> csrf.disable())
//
//                // 🔐 Authorization rules
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers("/api/auth/**").permitAll()          // public
//                        .requestMatchers("/api/products/**").permitAll()      // public
//                        .requestMatchers("/api/loans/admin/**").hasRole("ADMIN") // admin only
//                        .requestMatchers("/api/loans/**").authenticated()     // ROLE_USER or ROLE_ADMIN
//                        .anyRequest().authenticated()
//                )
//
//                // 🧩 Exception handlers (avoid all errors becoming 403)
//                .exceptionHandling(ex -> ex
//                        .authenticationEntryPoint((req, res, e) -> {
//                            res.setStatus(401);
//                            res.setContentType("application/json");
//                            res.getWriter().write("{\"error\":\"Unauthorized\"}");
//                        })
//                        .accessDeniedHandler((req, res, e) -> {
//                            res.setStatus(403);
//                            res.setContentType("application/json");
//                            res.getWriter().write("{\"error\":\"Forbidden\"}");
//                        })
//                )
//
//                // 🔥 Add JWT filter before UsernamePasswordAuthenticationFilter
//                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
//
//        return http.build();
//    }
//
//    // Required for authenticationManager.authenticate() used in Login API
//    @Bean
//    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
//        return configuration.getAuthenticationManager();
//    }
//}