package ru.ratauth.server.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static ru.ratauth.server.services.log.LogFields.RESPONSE_PAYLOAD;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ResponseLogger {
    private ObjectMapper objectMapper;

    @SneakyThrows
    public <T> void logResponse(T response) {
        MDC.put(RESPONSE_PAYLOAD.val(), objectMapper.writeValueAsString(response));
    }
}
