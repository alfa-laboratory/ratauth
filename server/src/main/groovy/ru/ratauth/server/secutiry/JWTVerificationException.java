package ru.ratauth.server.secutiry;

/**
 * @author mgorelikov
 * @since 11/11/15
 */
public class JWTVerificationException extends RuntimeException {

    public JWTVerificationException() {
    }

    public JWTVerificationException(String message) {
        super(message);
    }

    public JWTVerificationException(String message, Throwable cause) {
        super(message, cause);
    }

    public JWTVerificationException(Throwable cause) {
        super(cause);
    }

    public JWTVerificationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
