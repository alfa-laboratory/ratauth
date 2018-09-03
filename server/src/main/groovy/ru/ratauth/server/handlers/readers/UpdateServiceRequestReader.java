package ru.ratauth.server.handlers.readers;

import ru.ratauth.interaction.UpdateServiceRequest;

import static java.lang.Boolean.parseBoolean;

public class UpdateServiceRequestReader {
    public static UpdateServiceRequest readUpdateServiceRequest(RequestReader params) {
        return UpdateServiceRequest.builder()
            .clientId(params.removeField("client_id", true))
            .code(params.removeField("update_code", true))
            .skip(parseBoolean(params.removeField("skip", true)))
            .updateService(params.removeField("update_service", true))
            .reason(params.removeField("reason", true))
            .data(params.toMap())
            .build();
    }
}
