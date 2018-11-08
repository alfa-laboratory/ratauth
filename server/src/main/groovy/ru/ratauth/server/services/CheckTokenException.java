package ru.ratauth.server.services;

/**
 * @author mgorelikov
 * @since 10/11/15
 */
public class CheckTokenException extends RuntimeException {
    public CheckTokenException() {
    }

    public CheckTokenException(String message) {
        super(message);
    }

    public CheckTokenException(String message, Throwable cause) {
        super(message, cause);
    }

    public CheckTokenException(Throwable cause) {
        super(cause);
    }

    public CheckTokenException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
