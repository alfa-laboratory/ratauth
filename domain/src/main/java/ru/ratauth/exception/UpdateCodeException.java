package ru.ratauth.exception;

public class UpdateCodeException extends BaseAuthServerException {

    private final String id;

    public UpdateCodeException(ID id) {
        super(id.baseText);
        this.id = id.name();
    }

    public UpdateCodeException(String id, String message) {
        super(message);
        this.id = id;
    }

    public UpdateCodeException(String id, String message, Throwable cause) {
        super(message, cause);
        this.id = id;
    }


    public UpdateCodeException(String id, Throwable cause) {
        super(cause);
        this.id = id;
    }


    public enum ID {
        UPDATE_CODE_ENTRY_NOT_FOUND("Wrong update code, entry not found"),
        UPDATE_CODE_ALREADY_USED("Update code was already used"),
        UPDATE_CODE_ENTRY_EXPIRED("Update code entry expired");

        private final String baseText;

        ID(String baseText) {
            this.baseText = baseText;
        }

        public String getBaseText() {
            return baseText;
        }
    }
}
