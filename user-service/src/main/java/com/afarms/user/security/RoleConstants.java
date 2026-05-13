package com.afarms.user.security;

import java.util.Set;

public final class RoleConstants {

    public static final String DEFAULT_ROLE = "USER";
    public static final String ADMIN = "ADMIN";
    public static final String MASTER = "MASTER";
    public static final String SUB_USER = "SUB_USER";
    public static final Set<String> ALLOWED_ROLES = Set.of(DEFAULT_ROLE, ADMIN, MASTER, SUB_USER);

    private RoleConstants() {
    }
}
