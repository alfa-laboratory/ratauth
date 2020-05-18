package ru.ratauth.server.handlers

import groovy.transform.CompileStatic
import ratpack.http.client.HttpClient

@CompileStatic
class HttpClientHolder {

    static HttpClient instance

    static setInstance(HttpClient httpClient) {
        instance = httpClient
    }

    static HttpClient getInstance() {
        if (instance == null) {
            throw new IllegalStateException("The instance was not initialized")
        }
        return instance
    }

}
