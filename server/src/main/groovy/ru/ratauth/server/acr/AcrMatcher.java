package ru.ratauth.server.acr;

import ratpack.http.Request;

public interface AcrMatcher {

    String match(Request request);

}
