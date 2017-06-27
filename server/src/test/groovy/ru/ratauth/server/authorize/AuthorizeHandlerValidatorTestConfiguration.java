package ru.ratauth.server.authorize;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import ratpack.spring.config.EnableRatpack;
import ru.ratauth.server.acr.AcrResolverConfiguration;
import ru.ratauth.server.authcode.AuthCodeServiceTestConfiguration;
import ru.ratauth.server.jwt.JWTConfiguration;
import ru.ratauth.server.jwt.JWTDecoder;
import ru.ratauth.server.utils.AuthorizeHandlerValidator;

@Configuration
@Import(JWTConfiguration.class)
public class AuthorizeHandlerValidatorTestConfiguration {

    @Bean
    public AuthorizeHandlerValidator authorizeHandlerValidator(JWTDecoder jwtDecoder) {
        return new AuthorizeHandlerValidator(jwtDecoder);
    }

}
