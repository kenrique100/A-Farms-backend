package com.afarms.user.security;

import com.afarms.user.model.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
public class JwtUtil {

    private final SecretKey secretKey;

    public JwtUtil(@Value("${security.jwt.secret}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(User user) {
        String normalizedRole = RoleConstants.normalizeRole(user.getRole());
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

    public boolean isTokenValid(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT token has expired: {}", e.getMessage());
        } catch (JwtException e) {
            log.warn("JWT token is invalid: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected JWT validation error: {}", e.getMessage());
        }
        return false;
    }

    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    public UUID extractUserId(String token) {
        try {
            String userId = extractAllClaims(token).get("userId", String.class);
            return userId != null ? UUID.fromString(userId) : null;
        } catch (Exception e) {
            return null;
        }
    }

    public UUID extractFarmId(String token) {
        try {
            String farmId = extractAllClaims(token).get("farmId", String.class);
            return farmId == null || farmId.isBlank() ? null : UUID.fromString(farmId);
        } catch (Exception e) {
            return null;
        }
    }

    public String extractRole(String token) {
        try {
            return extractAllClaims(token).get("role", String.class);
        } catch (Exception e) {
            return RoleConstants.DEFAULT_ROLE;
        }
    }

    public List<String> extractRoles(String token) {
        try {
            // Safe typed extraction — avoids unchecked cast warning
            Object raw = extractAllClaims(token).get("roles");
            if (raw instanceof List<?> list) {
                return list.stream()
                        .filter(item -> item instanceof String)
                        .map(String.class::cast)
                        .toList();
            }
            return List.of();
        } catch (Exception e) {
            return List.of();
        }
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