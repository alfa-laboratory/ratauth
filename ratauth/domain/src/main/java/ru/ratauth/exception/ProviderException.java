package ru.ratauth.exception;

/**
 * @author mgorelikov
 * @since 15/03/16
 */
public class ProviderException extends BaseAuthServerException implements IdentifiedException {
    private final String id;

    public ProviderException(String id) {
        this.id = id;
    }

    public ProviderException(String id, String message) {
        super(message);
        this.id = id;
    }

    public ProviderException(ID id, String message) {
        super(message);
        this.id = id.getBaseText();
    }

    public ProviderException(String id, String message, Throwable cause) {
        super(message, cause);
        this.id = id;
    }

    public ProviderException(String id, Throwable cause) {
        super(cause);
        this.id = id;
    }

    @Override
    public String getTypeId() {
        return Type.PROVIDER.name();
    }

    @Override
    public String getId() {
        return id;
    }

    public enum ID {
        DESERIALIZATION_ERROR("Can't deserialize error response"),
        CONTRACT_VIOLATION("Contract violation");

        private final String baseText;

        ID(String baseText) {
            this.baseText = baseText;
        }

        public String getBaseText() {
            return baseText;
        }
    }

}
