package ru.ratauth.exception;

public class MissingProviderException extends RuntimeException {

    public MissingProviderException(String name) {
        super("Missing provider " + name);
    }

}
