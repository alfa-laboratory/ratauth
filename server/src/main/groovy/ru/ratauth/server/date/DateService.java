package ru.ratauth.server.date;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Clock;
import java.time.LocalDateTime;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DateService {

    private final Clock clock;

    public LocalDateTime now() {
        return LocalDateTime.now(clock);
    }

}
