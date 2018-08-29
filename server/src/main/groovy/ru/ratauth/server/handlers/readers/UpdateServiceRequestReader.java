package ru.ratauth.server.handlers.readers;

import ratpack.http.Headers;
import ratpack.util.MultiValueMap;
import ru.ratauth.interaction.UpdateServiceRequest;

public class UpdateServiceRequestReader {
    public static UpdateServiceRequest readUpdateServiceRequest(MultiValueMap<String, String> params, Headers headers) {
        return UpdateServiceRequest.builder().build();
    }
}
