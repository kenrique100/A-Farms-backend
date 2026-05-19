package com.afarms.user.utils;

import com.afarms.user.exception.BusinessException;

public final class PasswordValidator {

    private PasswordValidator() {}

    public static void validatePasswordStrength(String password) {
        if (password == null || password.length() < 6) {
            throw new BusinessException("Password must be at least 6 characters long");
        }

        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
        }

        if (!hasUpper || !hasLower || !hasDigit) {
            throw new BusinessException("Password must contain at least one uppercase letter, one lowercase letter, and one digit");
        }
    }
}