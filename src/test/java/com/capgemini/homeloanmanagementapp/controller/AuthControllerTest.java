package com.capgemini.homeloanmanagementapp.controller;

import com.capgemini.homeloanmanagementapp.dto.AuthResponse;
import com.capgemini.homeloanmanagementapp.dto.LoginRequest;
import com.capgemini.homeloanmanagementapp.dto.RegisterRequest;
import com.capgemini.homeloanmanagementapp.model.User;
import com.capgemini.homeloanmanagementapp.security.JwtService;
import com.capgemini.homeloanmanagementapp.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class AuthControllerTest {

    private AuthenticationManager authManager;
    private JwtService jwtService;
    private UserService userService;

    private AuthController controller;

    @BeforeEach
    void setup() {
        authManager = mock(AuthenticationManager.class);
        jwtService = mock(JwtService.class);
        userService = mock(UserService.class);
        controller = new AuthController(authManager, jwtService, userService);
    }

    // -----------------------------------------------------------
    // REGISTER SUCCESS
    // -----------------------------------------------------------
    @Test
    void testRegisterSuccess() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("john");
        req.setPassword("pass123");
        req.setFullName("John Doe");
        req.setEmail("john@test.com");

        User savedUser = User.builder()
                .id(1L)
                .username("john")
                .password("encodedPass")
                .fullName("John Doe")
                .email("john@test.com")
                .roles(Set.of())
                .build();

        Authentication fakeAuth = new UsernamePasswordAuthenticationToken("john", null, Set.of(() -> "ROLE_USER"));

        when(userService.registerCustomer(eq("john"), eq("pass123"), eq("John Doe"), eq("john@test.com")))
                .thenReturn(savedUser);
        when(authManager.authenticate(any())).thenReturn(fakeAuth);
        when(jwtService.generate(eq("john"), any())).thenReturn("jwt-token");

        ResponseEntity<AuthResponse> res = controller.register(req);

        assertNotNull(res);
        assertEquals("john", res.getBody().getUsername());
        assertEquals("jwt-token", res.getBody().getToken());
    }

    // -----------------------------------------------------------
    // REGISTER FAILURE (BadCredentialsException)
    // -----------------------------------------------------------
    @Test
    void testRegisterBadCredentials() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("john");
        req.setPassword("wrong");
        req.setFullName("John Doe");
        req.setEmail("john@test.com");

        when(userService.registerCustomer(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(User.builder().username("john").build());

        when(authManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("bad"));

        assertThrows(BadCredentialsException.class, () -> controller.register(req));
    }

    // -----------------------------------------------------------
    // REGISTER FAILURE (unexpected exception)
    // -----------------------------------------------------------
    @Test
    void testRegisterUnexpectedException() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("john");
        req.setPassword("pass123");
        req.setFullName("John Doe");
        req.setEmail("john@test.com");

        when(userService.registerCustomer(anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("db error"));

        assertThrows(RuntimeException.class, () -> controller.register(req));
    }

    // -----------------------------------------------------------
    // LOGIN SUCCESS
    // -----------------------------------------------------------
    @Test
    void testLoginSuccess() {
        LoginRequest req = new LoginRequest();
        req.setUsername("john");
        req.setPassword("pass123");

        Authentication fakeAuth = new UsernamePasswordAuthenticationToken("john", null, Set.of(() -> "ROLE_USER"));

        when(authManager.authenticate(any())).thenReturn(fakeAuth);
        when(jwtService.generate(eq("john"), any())).thenReturn("jwt-token");

        ResponseEntity<AuthResponse> res = controller.login(req);

        assertNotNull(res);
        assertEquals("john", res.getBody().getUsername());
        assertEquals("jwt-token", res.getBody().getToken());
    }

    // -----------------------------------------------------------
    // LOGIN FAILURE (BadCredentialsException)
    // -----------------------------------------------------------
    @Test
    void testLoginBadCredentials() {
        LoginRequest req = new LoginRequest();
        req.setUsername("john");
        req.setPassword("wrong");

        when(authManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("bad"));

        assertThrows(BadCredentialsException.class, () -> controller.login(req));
    }

    // -----------------------------------------------------------
    // LOGIN FAILURE (unexpected exception)
    // -----------------------------------------------------------
    @Test
    void testLoginUnexpectedException() {
        LoginRequest req = new LoginRequest();
        req.setUsername("john");
        req.setPassword("pass123");

        when(authManager.authenticate(any()))
                .thenThrow(new RuntimeException("unexpected"));

        assertThrows(RuntimeException.class, () -> controller.login(req));
    }
   /* @Test
    void register_whenServiceThrowsException_returnsError() {
        AuthenticationManager auth = mock(AuthenticationManager.class);
        JwtService jwt = mock(JwtService.class);
        UserService userService = mock(UserService.class);

        AuthController controller = new AuthController(auth, jwt, userService);

        RegisterRequest req = new RegisterRequest();
        req.setUsername("existing");
        req.setPassword("pass");
        req.setEmail("a@mail.com");
        req.setFullName("User");

        when(userService.registerCustomer(any(), any(), any(), any()))
                .thenThrow(new RuntimeException("User already exists"));

        assertThrows(RuntimeException.class, () -> controller.register(req));

        verify(userService).registerCustomer(any(), any(), any(), any());
    }*/

    @Test
    void register_whenServiceThrowsException_returnsError() {
        AuthenticationManager auth = mock(AuthenticationManager.class);
        JwtService jwt = mock(JwtService.class);
        UserService userService = mock(UserService.class);

        AuthController controller = new AuthController(auth, jwt, userService);

        RegisterRequest req = new RegisterRequest();
        req.setUsername("existing");
        req.setPassword("pass");
        req.setEmail("a@mail.com");
        req.setFullName("User");

        when(userService.registerCustomer(any(), any(), any(), any()))
                .thenThrow(new RuntimeException("User already exists"));

        assertThrows(RuntimeException.class, () -> controller.register(req));

        verify(userService).registerCustomer(any(), any(), any(), any());
    }

    @Test
    void login_whenBadCredentials_throwsException() {
        AuthenticationManager auth = mock(AuthenticationManager.class);
        JwtService jwt = mock(JwtService.class);
        UserService userService = mock(UserService.class);

        AuthController controller = new AuthController(auth, jwt, userService);

        LoginRequest req = new LoginRequest();
        req.setUsername("wrong");
        req.setPassword("invalid");

        when(auth.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class, () -> controller.login(req));

        verify(auth).authenticate(any());
    }

}