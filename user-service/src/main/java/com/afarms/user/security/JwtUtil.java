package com.afarms.user.security;


import com.afarms.user.model.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Component
public class JwtUtil {

    private final SecretKey secretKey;

    public JwtUtil(@Value("${security.jwt.secret}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(User user) {
        String normalizedRole = user.getRole() == null
                ? RoleConstants.DEFAULT_ROLE
                : user.getRole().trim().toUpperCase(Locale.ROOT);
        return Jwts.builder()
                .subject(user.getEmail())
                .claim("userId", user.getId().toString())
                .claim("farmId", user.getFarmId() == null ? null : user.getFarmId().toString())
                .claim("role", normalizedRole)
                .claim("roles", List.of(normalizedRole))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(secretKey)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    public UUID extractUserId(String token) {
        String userId = extractAllClaims(token).get("userId", String.class);
        return userId != null ? UUID.fromString(userId) : null;
    }

    public UUID extractFarmId(String token) {
        String farmId = extractAllClaims(token).get("farmId", String.class);
        return farmId == null || farmId.isBlank() ? null : UUID.fromString(farmId);
    }

    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    public List<String> extractRoles(String token) {
        List<String> roles = extractAllClaims(token).get("roles", List.class);
        return roles == null ? List.of() : roles;
    }

    public Map<String, Object> extractClaims(String token) {
        return extractAllClaims(token);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
