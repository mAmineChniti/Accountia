package com.accountia.expense.exception;

public class BusinessServiceUnavailableException extends RuntimeException {
    public BusinessServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }

    public BusinessServiceUnavailableException(String message) {
        super(message);
    }
}
