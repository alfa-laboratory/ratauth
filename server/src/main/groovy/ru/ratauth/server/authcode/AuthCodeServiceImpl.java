package ru.ratauth.server.authcode;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.ratauth.server.date.DateService;
import ru.ratauth.server.scope.Scope;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AuthCodeServiceImpl implements AuthCodeService {

    private final AuthCodeProperties authCodeProperties;
    private final DateService dateService;

    public AuthCode createAuthCode(Scope scope) {
        LocalDateTime expiresInSecond = dateService.now().plusSeconds(authCodeProperties.getExpiresInSecond());

        return AuthCode.builder()
                .expiresIn(expiresInSecond)
                .scope(scope)
                .build();
    }

}
