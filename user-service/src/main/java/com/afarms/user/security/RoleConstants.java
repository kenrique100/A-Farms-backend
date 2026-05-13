package com.afarms.user.security;

import java.util.Set;
import java.util.stream.StreamSupport;

public final class RoleConstants {

    public static final String DEFAULT_ROLE = "USER";
    public static final String ADMIN = "ADMIN";
    public static final String MASTER = "MASTER";
    public static final String SUB_USER = "SUB_USER";
    public static final Set<String> ALLOWED_ROLES = Set.of(DEFAULT_ROLE, ADMIN, MASTER, SUB_USER);
    private static final java.util.List<String> ROLE_PRIORITY = java.util.List.of(ADMIN, MASTER, SUB_USER, DEFAULT_ROLE);

    private RoleConstants() {
    }

    public static String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            return DEFAULT_ROLE;
        }
        return role.trim().toUpperCase(java.util.Locale.ROOT);
    }

    public static String resolvePrimaryRole(Iterable<String> roles) {
        Set<String> normalized = StreamSupport.stream(roles.spliterator(), false)
                .map(RoleConstants::normalizeRole)
                .filter(ALLOWED_ROLES::contains)
                .collect(java.util.stream.Collectors.toSet());
        return ROLE_PRIORITY.stream()
                .filter(normalized::contains)
                .findFirst()
                .orElse(DEFAULT_ROLE);
    }
}
