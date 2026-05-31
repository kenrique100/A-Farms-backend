package com.afarms.transaction.utils;

import com.afarms.transaction.exception.TransactionException;
import org.springframework.http.HttpStatus;

public class ValidationUtils {

    private ValidationUtils() {}

    public static void validateRange(int range) {
        if (range <= 0) {
            throw new TransactionException("Range must be greater than zero", HttpStatus.BAD_REQUEST);
        }
    }
}