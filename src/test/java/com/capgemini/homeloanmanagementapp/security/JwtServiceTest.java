package com.capgemini.homeloanmanagementapp.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwt;

    private static final String SECRET =
            "012345678901234567890123456789012345678901234567890123456789AB"; // 64 bytes

    @BeforeEach
    void setup() {
        // Large TTL so tests NEVER fail from accidental expiration
        jwt = new JwtService(SECRET, 60_000); // 1 minute TTL
    }

    // -----------------------------------------------------------
    // 1. Valid token: generate + extract username + extract roles
    // -----------------------------------------------------------
    @Test
    void generate_extractUsername_extractRoles_workCorrectly() {
        String token = jwt.generate("alice", new String[]{"ROLE_USER", "ROLE_ADMIN"});

        assertNotNull(token);
        assertEquals("alice", jwt.extractUsername(token));

        String[] roles = jwt.extractRoles(token);
        assertEquals(2, roles.length);
        assertTrue(Arrays.asList(roles).contains("ROLE_USER"));
        assertTrue(Arrays.asList(roles).contains("ROLE_ADMIN"));

        assertTrue(jwt.isValid(token));
    }

    // -----------------------------------------------------------
    // 2. Invalid signature
    // -----------------------------------------------------------
    @Test
    void tokenWithWrongSecret_isInvalid_andThrowsSignatureException() {
        JwtService other = new JwtService(
                "999999999999999999999999999999999999999999999999999999999999XY",
                60000
        );

        String token = jwt.generate("bob", new String[]{"A"});

        assertFalse(other.isValid(token));
        assertThrows(SignatureException.class, () -> other.extractUsername(token));
        assertThrows(SignatureException.class, () -> other.extractRoles(token));
    }

    // -----------------------------------------------------------
    // 3. Malformed token
    // -----------------------------------------------------------
    @Test
    void malformedToken_invalid() {
        String bad = "NOT_A_JWT";

        assertFalse(jwt.isValid(bad));
        assertThrows(MalformedJwtException.class, () -> jwt.extractUsername(bad));
        assertThrows(MalformedJwtException.class, () -> jwt.extractRoles(bad));
    }

    // -----------------------------------------------------------
    // 4. Expired token branch
    // -----------------------------------------------------------
    @Test
    void expiredToken_invalid() throws Exception {
        JwtService shortLived = new JwtService(SECRET, 5); // 5ms TTL

        String token = shortLived.generate("temp", new String[]{"X"});

        Thread.sleep(15); // Let it expire

        assertFalse(shortLived.isValid(token));
        assertThrows(ExpiredJwtException.class, () -> shortLived.extractUsername(token));
        assertThrows(ExpiredJwtException.class, () -> shortLived.extractRoles(token));
    }

    // -----------------------------------------------------------
    // 5. Force internal parse() error -> generic error path
    // -----------------------------------------------------------
    @Test
    void parseError_genericExceptionPath() throws Exception {
        JwtService broken = new JwtService(SECRET, 60000);

        Field keyField = JwtService.class.getDeclaredField("key");
        keyField.setAccessible(true);
        keyField.set(broken, null);  // Force NullPointerException in parser

        assertFalse(broken.isValid("anything"));
    }

    @Test
    void extractRoles_whenNoRolesClaim_returnsEmptyArray() {
        JwtService jwt = new JwtService(SECRET, 60_000);

        // Build a valid token WITHOUT any 'roles' claim
        var key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        Date now = new Date();
        Date exp = new Date(now.getTime() + 60_000);
        String token = Jwts.builder()
                .setSubject("alice")
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        String[] roles = jwt.extractRoles(token);

        assertNotNull(roles);
        assertEquals(0, roles.length); // branch where roles == null in claims
    }

    // 2) generate: roles == null → triggers the null-branch and throws NPE at String.join
    @Test
    void generate_whenRolesNull_throwsNullPointerException() {
        JwtService jwt = new JwtService(SECRET, 60_000);

        assertThrows(NullPointerException.class, () ->
                jwt.generate("bob", null) // roles null → exercises the other branch
        );
    }

}