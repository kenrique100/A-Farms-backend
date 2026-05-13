package com.afarms.user.config;

import com.afarms.user.security.JwtUtil;
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
import java.util.Locale;
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
                log.debug("Authenticated request for {} with role {}", email, role);
            } else {
                log.warn("Rejected request with invalid JWT for path {}", request.getRequestURI());
            }
        }
        chain.doFilter(request, response);
    }

    private String resolveRole(String token) {
        List<String> roles = jwtUtil.extractRoles(token);
        if (!roles.isEmpty()) {
            return roles.getFirst().toUpperCase(Locale.ROOT);
        }

        String role = jwtUtil.extractRole(token);
        if (role != null && !role.isBlank()) {
            return role.trim().toUpperCase(Locale.ROOT);
        }

        Map<String, Object> claims = jwtUtil.extractClaims(token);
        Object realmAccess = claims.get("realm_access");
        if (realmAccess instanceof Map<?, ?> realmAccessMap) {
            Object realmRoles = realmAccessMap.get("roles");
            if (realmRoles instanceof Collection<?> collection) {
                return collection.stream()
                        .filter(Objects::nonNull)
                        .map(String::valueOf)
                        .map(String::trim)
                        .filter(value -> !value.isBlank())
                        .map(value -> value.toUpperCase(Locale.ROOT))
                        .findFirst()
                        .orElse("USER");
            }
        }
        return "USER";
    }
}
