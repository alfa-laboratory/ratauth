package ru.ratauth.exception;

/**
 * @author djassan
 * @since 15/03/16
 */
public interface IdentifiedException extends BaseIdentifiedException {
    String getTypeId();

    String getId();

    String getMessage();
}
