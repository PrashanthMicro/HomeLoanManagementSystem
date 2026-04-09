package com.capgemini.homeloanmanagementapp.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j; // <-- added
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Map;

@Slf4j // <-- added
@Service
public class JwtService {
    private final Key key;
    private final long expirationMs;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
        log.info("JwtService initialized (expirationMs={} ms)", expirationMs); // <-- added
    }

    public String generate(String username, String[] roles) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expirationMs);
        log.debug("Generating JWT for user={} with rolesCount={} (exp at {})",
                username, roles != null ? roles.length : 0, exp); // <-- added

        String token = Jwts.builder()
                .setSubject(username)
                .addClaims(Map.of("roles", String.join(",", roles)))
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        log.info("JWT generated for user={} (length={})", username, token != null ? token.length() : 0); // <-- added
        return token;
    }

    public String extractUsername(String token) {
        try {
            String sub = parse(token).getBody().getSubject();
            log.debug("extractUsername: subject={}", sub); // <-- added
            return sub;
        } catch (JwtException e) {
            log.warn("extractUsername failed: {}", e.getMessage()); // <-- added
            throw e;
        }
    }

    public String[] extractRoles(String token) {
        try {
            String roles = (String) parse(token).getBody().get("roles");
            String[] arr = roles == null ? new String[]{} : roles.split(",");
            log.debug("extractRoles: rolesCount={}", arr.length); // <-- added
            return arr;
        } catch (JwtException e) {
            log.warn("extractRoles failed: {}", e.getMessage()); // <-- added
            throw e;
        }
    }

    public boolean isValid(String token) {
        try {
            parse(token); // throws on invalid/expired
            log.debug("JWT is valid"); // <-- added
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT expired at {}", e.getClaims().getExpiration()); // <-- added
            return false;
        } catch (JwtException e) {
            log.warn("JWT invalid: {}", e.getMessage()); // <-- added
            return false;
        } catch (Exception e) {
            log.warn("JWT validation error: {}", e.getMessage()); // <-- added
            return false;
        }
    }

    private Jws<Claims> parse(String token) {
        // Keep this method centralized for consistent parsing + logging
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
    }
}