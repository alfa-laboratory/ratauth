package ru.ratauth.server.secutiry;


import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OAuthIssuerImpl implements OAuthIssuer {

    private final ValueGenerator vg;

    @Override
    @SneakyThrows
    public String accessToken() {
        return vg.generateValue();
    }

    @Override
    @SneakyThrows
    public String refreshToken() {
        return vg.generateValue();
    }

    @Override
    @SneakyThrows
    public String authorizationCode() {
        return vg.generateValue();
    }

    @Override
    @SneakyThrows
    public String mfaToken() {
        return vg.generateValue();
    }

}
