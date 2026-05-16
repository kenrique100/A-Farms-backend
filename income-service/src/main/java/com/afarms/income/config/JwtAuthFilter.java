package com.afarms.income.config;

import com.afarms.income.security.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
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
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        if (shouldSkipJwtValidation(requestURI)) {
            chain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtUtil.validateToken(token)) {
                String email = jwtUtil.extractEmail(token);
                String role = resolveRole(token);
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(email, null,
                                List.of(new SimpleGrantedAuthority("ROLE_" + role)));
                SecurityContextHolder.getContext().setAuthentication(auth);
            } else {
                log.warn("Rejected request with invalid JWT for path {}", requestURI);
            }
        }

        chain.doFilter(request, response);
    }

    private boolean shouldSkipJwtValidation(String requestURI) {
        String[] skipPaths = {
                "/swagger-ui",
                "/swagger-ui.html",
                "/v3/api-docs",
                "/swagger-resources",
                "/webjars",
                "/actuator/health",
                "/actuator/info"
        };

        for (String path : skipPaths) {
            if (requestURI.startsWith(path)) {
                return true;
            }
        }
        return false;
    }

    private String resolveRole(String token) {
        List<String> roles = jwtUtil.extractRoles(token);
        if (!roles.isEmpty()) {
            return normalizeRole(roles.get(0));
        }

        String role = jwtUtil.extractRole(token);
        if (role != null && !role.isBlank()) {
            return normalizeRole(role);
        }

        Map<String, Object> claims = jwtUtil.extractClaims(token);
        Object realmAccess = claims.get("realm_access");
        if (realmAccess instanceof Map<?, ?> realmAccessMap) {
            Object realmRoles = realmAccessMap.get("roles");
            if (realmRoles instanceof Collection<?> collection) {
                return collection.stream()
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
