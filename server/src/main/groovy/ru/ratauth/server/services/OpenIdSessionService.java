package ru.ratauth.server.services;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.ratauth.entities.*;
import ru.ratauth.exception.AuthorizationException;
import ru.ratauth.interaction.TokenRequest;
import ru.ratauth.providers.Fields;
import ru.ratauth.providers.auth.dto.BaseAuthFields;
import ru.ratauth.server.secutiry.OAuthIssuer;
import ru.ratauth.server.secutiry.TokenProcessor;
import ru.ratauth.server.services.log.ActionLogger;
import ru.ratauth.server.utils.DateUtils;
import ru.ratauth.services.SessionService;
import rx.Observable;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static java.time.LocalDateTime.now;

/**
 * @author djassan
 * @since 17/02/16
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OpenIdSessionService implements AuthSessionService {

    public static final String RATAUTH = "ratauth";
    private final SessionService sessionService;
    private final TokenProcessor tokenProcessor;
    private final TokenCacheService tokenCacheService;
    private final OAuthIssuer codeGenerator;
    private final ActionLogger actionLogger;

    @Value("${auth.master_secret}")
    private String masterSecret;//final

    @Value("${auth.session.tokens_limit:0}")
    private int tokensLimit;

    @Value("${auth.session.mfa-ttl:3600}")
    private int mfaTokenTTL;

    @Override
    public Observable<Session> initSession(RelyingParty relyingParty, Map<String, Object> userInfo, Set<String> scopes, AcrValues acrValues,
                                           String redirectUrl) {
        return createSession(relyingParty, userInfo, scopes, acrValues, redirectUrl, now(), null);
    }

    @Override
    public Observable<Session> createSession(RelyingParty relyingParty, Map<String, Object> userInfo, Set<String> scopes, AcrValues acrValues,
                                             String redirectUrl) {
        LocalDateTime now = now();
        final LocalDateTime tokenExpires = now.plus(relyingParty.getTokenTTL(), ChronoUnit.SECONDS);
        final LocalDateTime refreshTokenExpires = now.plus(relyingParty.getRefreshTokenTTL(), ChronoUnit.SECONDS);

        final Token token = Token.builder()
                .refreshToken(codeGenerator.refreshToken())
                .refreshTokenExpiresIn(DateUtils.fromLocal(refreshTokenExpires))
                .refreshCreated(DateUtils.fromLocal(now))
                .token(codeGenerator.accessToken())
                .expiresIn(DateUtils.fromLocal(tokenExpires))
                .created(DateUtils.fromLocal(now))
                .lifeType(TokenLifeType.MAIN)
                .build();
        return createSession(relyingParty, userInfo, scopes, acrValues, redirectUrl, now, token);
    }

    @Override
    public Observable<Session> updateSession(RelyingParty relyingParty, Session session, Map<String, String> additionalUserInfo){
        Map<String, Object> oldJwtToken = new HashMap<>();
        oldJwtToken.putAll(tokenProcessor.extractInfo(session.getUserInfo(), masterSecret));
        oldJwtToken.putAll(additionalUserInfo);
        Set<String> scopes = new HashSet<>((List<String>) oldJwtToken.get("scope"));
        scopes.add(relyingParty.getName());
        Set<String> acrValues = new HashSet<>((List<String>) oldJwtToken.get("acr_values"));
        return updateIdToken(session, new UserInfo(oldJwtToken), scopes, acrValues).map(b -> session);
    }

    private Observable<Session> createSession(RelyingParty relyingParty, Map<String, Object> userInfo, Set<String> scopes,
                                              AcrValues acrValues, String redirectUrl, LocalDateTime now, Token token) {
        final LocalDateTime sessionExpires = now.plus(relyingParty.getSessionTTL(), ChronoUnit.SECONDS);
        final LocalDateTime refreshExpires = now.plus(relyingParty.getRefreshTokenTTL(), ChronoUnit.SECONDS);
        final LocalDateTime authCodeExpires = now.plus(relyingParty.getCodeTTL(), ChronoUnit.SECONDS);
        final LocalDateTime mfaTokenExpires = now.plus(mfaTokenTTL, ChronoUnit.SECONDS);

        final String jwtInfo = tokenProcessor.createToken(RATAUTH, masterSecret, null,
                DateUtils.fromLocal(now), DateUtils.fromLocal(sessionExpires),
                tokenCacheService.extractAudience(scopes), scopes, acrValues.getValues(), userInfo.get(Fields.USER_ID.val()).toString(), userInfo);

        final AuthEntry authEntry = AuthEntry.builder()
                .created(DateUtils.fromLocal(now))
                .authCode(codeGenerator.authorizationCode())
                .codeExpiresIn(DateUtils.fromLocal(authCodeExpires))
                .refreshToken(codeGenerator.refreshToken())
                .refreshTokenExpiresIn(DateUtils.fromLocal(refreshExpires))
                .scopes(scopes)
                .relyingParty(relyingParty.getName())
                .authType(AuthType.COMMON)
                .redirectUrl(redirectUrl)
                .build();
        authEntry.addToken(token);
        final Session session = Session.builder()
                .sessionToken(codeGenerator.refreshToken())
                .mfaToken(codeGenerator.mfaToken())
                .mfaTokenExpiresIn(DateUtils.fromLocal(mfaTokenExpires))
                .receivedAcrValues(AcrValues.valueOf(acrValues.getFirst()))
                .identityProvider(relyingParty.getIdentityProvider())
                .authClient(relyingParty.getName())
                .status(Status.ACTIVE)
                .created(DateUtils.fromLocal(now))
                .expiresIn(DateUtils.fromLocal(sessionExpires))
                .userId((String) userInfo.get(BaseAuthFields.USER_ID.val()))
                .userInfo(jwtInfo)
                .entries(new HashSet<>(Arrays.asList(authEntry)))
                .build();
        return sessionService.create(session)
                .doOnNext(actionLogger::addSessionInfo);
    }

    @Override
    public Observable<Boolean> addToken(TokenRequest oauthRequest, Session session, RelyingParty relyingParty, boolean needUpdateRefresh) {
        final LocalDateTime now = now();
        final LocalDateTime tokenExpires = now.plus(relyingParty.getTokenTTL(), ChronoUnit.SECONDS);
        final LocalDateTime refreshTokenExpiresIn = generateRefreshTokenExpiresIn(tokenExpires, relyingParty, needUpdateRefresh);
        final Token token = Token.builder()
                .refreshToken(codeGenerator.refreshToken())
                .refreshTokenExpiresIn(DateUtils.fromLocal(refreshTokenExpiresIn))
                .refreshCreated(DateUtils.fromLocal(now))
                .token(codeGenerator.accessToken())
                .lifeType(needUpdateRefresh ? TokenLifeType.MAIN : TokenLifeType.REISSUED)
                .expiresIn(DateUtils.fromLocal(tokenExpires))
                .created(DateUtils.fromLocal(now))
                .build();
        return sessionService.addToken(session, relyingParty.getName(), token)
                .map(b -> {
                    session.getEntry(relyingParty.getName())
                            .map(this::checkTokensCount)
                            .ifPresent(entry -> entry.addToken(token));
                    return b;
                });
    }

    private AuthEntry checkTokensCount(AuthEntry authEntry) {
        if (tokensLimit > 0 && authEntry.getTokens().size() > tokensLimit) {
            throw new IllegalStateException("Session cross tokens threshold");
        }
        return authEntry;
    }

    private LocalDateTime generateRefreshTokenExpiresIn(LocalDateTime tokenExpires, RelyingParty relyingParty, boolean needUpdateRefresh) {
        if (!needUpdateRefresh) {
            return tokenExpires;
        }
        return now().plus(relyingParty.getRefreshTokenTTL(), ChronoUnit.SECONDS);
    }

    @Override
    public Observable<Session> addEntry(Session session, RelyingParty relyingParty, Set<String> scopes, String redirectUrl) {
        final LocalDateTime now = now();
        final LocalDateTime refreshExpires = now.plus(relyingParty.getRefreshTokenTTL(), ChronoUnit.SECONDS);
        final LocalDateTime authCodeExpires = now.plus(relyingParty.getCodeTTL(), ChronoUnit.SECONDS);
        final AuthEntry authEntry = AuthEntry.builder()
                .created(DateUtils.fromLocal(now))
                .authCode(codeGenerator.authorizationCode())
                .codeExpiresIn(DateUtils.fromLocal(authCodeExpires))
                .refreshToken(codeGenerator.refreshToken())
                .refreshTokenExpiresIn(DateUtils.fromLocal(refreshExpires))
                .scopes(scopes)
                .relyingParty(relyingParty.getName())
                .authType(AuthType.CROSS)
                .redirectUrl(redirectUrl)
                .build();
        return sessionService.addEntry(session, authEntry)
                .doOnNext(res -> session.getEntries().add(authEntry))
                .map(res -> session);
    }

    @Override
    public Observable<Session> getByValidCode(String code, Date now) {
        return sessionService.getByValidCode(code, now)
                .doOnNext(actionLogger::addSessionInfo);
    }

    @Override
    public Observable<Session> getByValidRefreshToken(String token, Date now) {
        return sessionService.getByValidRefreshToken(token, now)
                .doOnNext(actionLogger::addSessionInfo);
    }

    @Override
    public Observable<Session> getByValidSessionToken(String token, Date now, boolean checkValidRefreshToken) {
        return sessionService.getByValidSessionToken(token, now, checkValidRefreshToken)
                .doOnNext(actionLogger::addSessionInfo);
    }

    @Override
    public Observable<Session> getByValidToken(String token, Date now) {
        return sessionService.getByValidToken(token, now)
                .doOnNext(actionLogger::addSessionInfo);
    }

    @Override
    public Observable<Session> getByValidMFAToken(String token, Date now) {
        return sessionService.getByValidMFAToken(token, now)
                .doOnNext(actionLogger::addSessionInfo);
    }

    @Override
    public Observable<Boolean> updateIdToken(Session session, UserInfo userInfo, Set<String> scopes, Set<String> authContext) {
        final String jwtInfo = tokenProcessor.createToken(RATAUTH, masterSecret, null,
                DateUtils.fromLocal(now()), session.getExpiresIn(),
                tokenCacheService.extractAudience(scopes), scopes, authContext, session.getUserId(), userInfo.toMap());
        session.setUserInfo(jwtInfo);
        return sessionService.updateUserInfo(session.getId(), jwtInfo);
    }

    @Override
    public Observable<Boolean> updateAcrValues(Session session) {
        return sessionService.updateAcrValues(session);
    }

    @Override
    public Observable<Boolean> updateAuthCodeExpired(String code, Date now) {
        return sessionService.updateAuthCodeExpired(code, now);
    }
}
