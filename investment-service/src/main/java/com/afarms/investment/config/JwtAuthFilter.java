package com.afarms.investment.config;

import com.afarms.investment.security.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

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
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "Missing or invalid Authorization header");
            return;
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "Invalid or expired token");
            return;
        }

        Claims claims = jwtUtil.extractClaims(token);
        String email = claims.getSubject();
        String role = resolveRole(claims);

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                email,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role)));

        SecurityContextHolder.getContext().setAuthentication(auth);
        chain.doFilter(request, response);
    }

    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(String.format(
                "{\"timestamp\":\"%s\",\"status\":%d,\"message\":\"%s\"}",
                LocalDateTime.now(), status, message));
    }

    private boolean shouldSkipJwtValidation(String requestURI) {
        for (String pattern : SecurityConfig.PUBLIC_ENDPOINTS) {
            if (pathMatcher.match(pattern, requestURI)) {
                return true;
            }
        }
        return false;
    }

    private String resolveRole(Claims claims) {
        Object rolesClaim = claims.get("roles");
        if (rolesClaim instanceof Collection<?> collection) {
            return collection.stream().filter(Objects::nonNull)
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
                return c.stream().filter(Objects::nonNull)
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
