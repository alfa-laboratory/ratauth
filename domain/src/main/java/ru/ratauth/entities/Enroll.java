package ru.ratauth.entities;

public interface Enroll extends Iterable<String> {

    String getFirst();

    AcrValues difference(AcrValues acrValues);

}