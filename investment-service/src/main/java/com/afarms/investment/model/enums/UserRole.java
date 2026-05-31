package com.afarms.investment.model.enums;

public enum UserRole {
    ADMIN, MASTER, SUB_USER, USER;

    public static boolean isWriteAllowed(String role) {
        return role != null && (role.equalsIgnoreCase(MASTER.name()) ||
                role.equalsIgnoreCase(SUB_USER.name()) ||
                role.equalsIgnoreCase(ADMIN.name()));
    }

    public static boolean isValid(String role) {
        if (role == null) return false;
        for (UserRole r : values()) {
            if (r.name().equalsIgnoreCase(role)) return true;
        }
        return false;
    }
}