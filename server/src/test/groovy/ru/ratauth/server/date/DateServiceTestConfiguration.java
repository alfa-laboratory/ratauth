package ru.ratauth.server.date;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.ratauth.server.date.DateService;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

@Configuration
public class DateServiceTestConfiguration {

    private static final long DATE_2000_01_01 = 946684800000L;
    private static final ZoneOffset ZONE_OFFSET = ZoneOffset.UTC;

    @Bean
    public Clock clock() {
        return Clock.fixed(Instant.ofEpochMilli(DATE_2000_01_01), ZONE_OFFSET);
    }

    @Bean
    public DateService dateService() {
        return new DateService(clock());
    }


}
