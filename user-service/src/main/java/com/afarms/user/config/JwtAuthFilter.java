package com.afarms.user.config;

import com.afarms.user.security.JwtUtil;
import com.afarms.user.security.RoleConstants;
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
import org.springframework.util.AntPathMatcher;
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
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws ServletException, IOException {

        String requestURI = request.getRequestURI();

        if (shouldSkipJwtValidation(requestURI)) {
            log.debug("Skipping JWT validation for public path: {}", requestURI);
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
            log.warn("Rejected request with invalid JWT for path {}", requestURI);
            sendUnauthorized(response, "Invalid or expired token");
            return;
        }

        String email = jwtUtil.extractEmail(token);
        String role = resolveRole(token);

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(email, null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + role)));
        SecurityContextHolder.getContext().setAuthentication(auth);
        log.debug("Authenticated request for {} with role {}", email, role);

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
        for (String pattern : SecurityConfig.PUBLIC_ENDPOINTS) {
            if (pathMatcher.match(pattern, requestURI)) {
                return true;
            }
        }
        return false;
    }

    private String resolveRole(String token) {
        List<String> roles = jwtUtil.extractRoles(token);
        if (!roles.isEmpty()) {
            return RoleConstants.resolvePrimaryRole(roles);
        }
        String role = jwtUtil.extractRole(token);
        if (role != null && !role.isBlank()) {
            return RoleConstants.normalizeRole(role);
        }
        Map<String, Object> claims = jwtUtil.extractClaims(token);
        Object realmAccess = claims.get("realm_access");
        if (realmAccess instanceof Map<?, ?> realmAccessMap) {
            Object realmRoles = realmAccessMap.get("roles");
            if (realmRoles instanceof Collection<?> collection) {
                List<String> roleValues = collection.stream()
                        .filter(Objects::nonNull)
                        .map(String::valueOf)
                        .toList();
                return RoleConstants.resolvePrimaryRole(roleValues);
            }
        }
        return RoleConstants.DEFAULT_ROLE;
    }
}