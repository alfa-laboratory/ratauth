package ru.ratauth.server.accesstoken;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import ru.ratauth.server.acr.AcrValue;
import ru.ratauth.server.scope.Scope;

import java.time.LocalDateTime;

@Builder
@Getter
public class AccessToken {

    @NonNull
    private final String id;

    @NonNull
    private final LocalDateTime expiredAt;

    @NonNull
    private final AcrValue acrValue;

    private final Scope scope;

}
