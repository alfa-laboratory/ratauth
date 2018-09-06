package ru.ratauth.exception;

public class UpdateFlowException extends BaseAuthServerException {

    private final String id;

    public UpdateFlowException(ID id) {
        super(id.baseText);
        this.id = id.name();
    }

    public UpdateFlowException(String id, String message) {
        super(message);
        this.id = id;
    }

    public UpdateFlowException(String id, String message, Throwable cause) {
        super(message, cause);
        this.id = id;
    }


    public UpdateFlowException(String id, Throwable cause) {
        super(cause);
        this.id = id;
    }


    public enum ID {
        UPDATE_DATA_ENTRY_NOT_FOUND("Entry not found, you have used wrong code or session token"),
        UPDATE_CODE_ALREADY_USED("Update code was already used"),
        UPDATE_CODE_ENTRY_EXPIRED("Update code entry expired"),
        UPDATE_CALL_SERVICE("Call external service return exception");

        private final String baseText;

        ID(String baseText) {
            this.baseText = baseText;
        }

        public String getBaseText() {
            return baseText;
        }
    }
}
