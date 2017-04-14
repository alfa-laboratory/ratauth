package ru.ratauth.server.secutiry;


import lombok.SneakyThrows;

public class OAuthIssuerImpl implements OAuthIssuer {

    private ValueGenerator vg;

    public OAuthIssuerImpl(ValueGenerator vg) {
        this.vg = vg;
    }

    @SneakyThrows
    public String accessToken()  {
        return vg.generateValue();
    }

    @SneakyThrows
    public String refreshToken()  {
        return vg.generateValue();
    }

    @SneakyThrows
    public String authorizationCode() {
        return vg.generateValue();
    }
}