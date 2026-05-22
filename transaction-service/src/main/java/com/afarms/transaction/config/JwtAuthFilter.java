package com.afarms.transaction.config;

import com.afarms.transaction.security.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();

        if (shouldSkipJwtValidation(requestURI)) {
            chain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing or invalid Authorization header for path: {}", requestURI);
            sendUnauthorized(response, "Missing or invalid Authorization header");
            return;
        }

        String token = authHeader.substring(7);

        if (!jwtUtil.isTokenValid(token)) {
            log.warn("Invalid or expired JWT token for path: {}", requestURI);
            sendUnauthorized(response, "Invalid or expired token");
            return;
        }

        Claims claims = jwtUtil.extractClaims(token);
        String email = claims.getSubject();
        String role = resolveRole(claims);

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                email,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role))
        );

        SecurityContextHolder.getContext().setAuthentication(auth);
        log.debug("Authenticated transaction request for user: {}, role: {}", email, role);

        chain.doFilter(request, response);
    }

    private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(String.format(
                "{\"timestamp\":\"%s\",\"status\":401,\"message\":\"%s\"}",
                LocalDateTime.now(), message
        ));
    }

    private boolean shouldSkipJwtValidation(String requestURI) {
        String[] publicPaths = {
                "/swagger-ui",
                "/swagger-ui.html",
                "/v3/api-docs",
                "/swagger-resources",
                "/webjars",
                "/actuator/health",
                "/actuator/info"
        };
        for (String path : publicPaths) {
            if (requestURI.startsWith(path)) {
                return true;
            }
        }
        return false;
    }

    private String resolveRole(Claims claims) {
        Object rolesClaim = claims.get("roles");
        if (rolesClaim instanceof Collection<?> collection) {
            return collection.stream()
                    .filter(Objects::nonNull)
                    .map(String::valueOf)
                    .findFirst()
                    .map(this::normalizeRole)
                    .orElse("USER");
        }
        String role = claims.get("role", String.class);
        if (role != null && !role.isBlank()) {
            return normalizeRole(role);
        }
        Object realmAccess = claims.get("realm_access");
        if (realmAccess instanceof Map<?, ?> map) {
            Object realmRoles = map.get("roles");
            if (realmRoles instanceof Collection<?> c) {
                return c.stream()
                        .filter(Objects::nonNull)
                        .map(String::valueOf)
                        .findFirst()
                        .map(this::normalizeRole)
                        .orElse("USER");
            }
        }
        return "USER";
    }

    private String normalizeRole(String role) {
        return role.trim().replace("ROLE_", "").toUpperCase();
    }
}