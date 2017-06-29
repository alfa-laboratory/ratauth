package ru.ratauth.server.extended.enroll;

public class MissingProviderException extends RuntimeException {

    public MissingProviderException(String name) {
        super("Missing provider " + name);
    }

}
