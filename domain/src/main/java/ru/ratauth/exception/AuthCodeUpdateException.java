package ru.ratauth.exception;

/**
 * @author sserdyuk
 * @since 17/07/18
 */
public class AuthCodeUpdateException extends BaseAuthServerException {

    private final String id;

    public AuthCodeUpdateException(String id) {
        this.id = id;
    }

    public AuthCodeUpdateException(ID id) {
        super(id.getBaseText());
        this.id = id.name();
    }

    public AuthCodeUpdateException(String id, String message) {
        super(message);
        this.id = id;
    }

    public AuthCodeUpdateException(String id, String message, Throwable cause) {
        super(message, cause);
        this.id = id;
    }

    public AuthCodeUpdateException(String id, Throwable cause) {
        super(cause);
        this.id = id;
    }

    public enum ID {
        AUTH_CODE_EXPIRED("Auth code hasn't updated");

        private final String baseText;

        ID(String baseText) {
            this.baseText = baseText;
        }

        public String getBaseText() {
            return baseText;
        }
    }
}
