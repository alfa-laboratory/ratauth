package ru.ratauth.server.secutiry;

public class OAuthIssuerImpl implements OAuthIssuer {

    private ValueGenerator vg;

    public OAuthIssuerImpl(ValueGenerator vg) {
        this.vg = vg;
    }

    public String accessToken() throws OAuthSystemException {
        return vg.generateValue();
    }

    public String refreshToken() throws OAuthSystemException {
        return vg.generateValue();
    }

    public String authorizationCode() throws OAuthSystemException {
        return vg.generateValue();
    }
}