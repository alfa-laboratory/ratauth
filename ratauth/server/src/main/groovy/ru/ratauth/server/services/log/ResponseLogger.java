package ru.ratauth.server.services.log;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static ru.ratauth.server.services.log.LogFields.RESPONSE_PAYLOAD;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class ResponseLogger {
    private final ObjectMapper jacksonObjectMapper;

    @SneakyThrows
    public <T> void logResponse(T response) {
        MDC.put(RESPONSE_PAYLOAD.val(), jacksonObjectMapper.writeValueAsString(response));
        log.info("Obtain response");
    }
}
