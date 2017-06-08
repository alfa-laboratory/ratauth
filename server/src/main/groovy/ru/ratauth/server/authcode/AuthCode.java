package ru.ratauth.server.authcode;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Wither;
import ru.ratauth.server.scope.Scope;

import java.time.LocalDateTime;

@Builder
@Wither
@Data
public class AuthCode {

    private final Scope scope;
    private final LocalDateTime expiresIn;

}
