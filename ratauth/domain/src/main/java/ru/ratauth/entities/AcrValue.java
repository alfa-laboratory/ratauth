package ru.ratauth.entities;

public interface AcrValue extends Iterable<String> {

    String getFirst();

    AcrValues difference(AcrValues acrValues);

}