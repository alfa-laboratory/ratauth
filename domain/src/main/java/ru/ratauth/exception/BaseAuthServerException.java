package ru.ratauth.exception;

/**
 * BASE
 * <p>
 * /**
 *
 * @author mgorelikov
 * @since 23/02/16
 */
public class BaseAuthServerException extends RuntimeException implements BaseIdentifiedException {

    private static final String MODULE_UUID = "16080bf6-dbe4-428e-b648-06739b59e920";

    @Override
    public String getBaseId() {
        return MODULE_UUID;
    }

    public BaseAuthServerException() {
    }

    public BaseAuthServerException(String message) {
        super(message);
    }

    public BaseAuthServerException(String message, Throwable cause) {
        super(message, cause);
    }

    public BaseAuthServerException(Throwable cause) {
        super(cause);
    }

    public enum Type {
        AUTHORIZATION,
        REGISTRATION,
        PROVIDER,
        EXPIRED
    }
}
