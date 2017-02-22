package ru.ratauth;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import ru.ratauth.server.RatAuthApplication;

@Configuration
@Import(RatAuthApplication.class)
public class RatAuthAutoConfiguration {

}