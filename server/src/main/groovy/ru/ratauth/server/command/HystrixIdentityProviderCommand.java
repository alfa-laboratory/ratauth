package ru.ratauth.server.command;

import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixObservableCommand;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import ratpack.exec.Promise;
import ratpack.http.MediaType;
import ratpack.http.client.HttpClient;
import ratpack.http.client.ReceivedResponse;
import ratpack.http.client.RequestSpec;
import ratpack.rx.RxRatpack;
import ru.ratauth.entities.UserInfo;
import ru.ratauth.server.services.log.LogHeader;
import rx.Observable;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.netflix.hystrix.HystrixCommandGroupKey.Factory.asKey;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Optional.ofNullable;

@Slf4j
public class HystrixIdentityProviderCommand extends HystrixObservableCommand<ReceivedResponse> {

    private final HttpClient httpClient;
    private final Map<String, Object> data;
    private final URI uri;

    private String login;
    private String password;

    public HystrixIdentityProviderCommand(@NonNull HttpClient httpClient,
                                          @NonNull Map<String, String> data,
                                          UserInfo userInfo,
                                          @NonNull String relyingParty,
                                          @NonNull String enroll,
                                          @NonNull String url,
                                          String login,
                                          String password,
                                          Integer timeout) throws MalformedURLException, URISyntaxException {
        this(createSetter(enroll, timeout), httpClient, data, userInfo, relyingParty, enroll, url);
        this.login = login;
        this.password = password;
    }

    private HystrixIdentityProviderCommand(Setter setter,
                                           @NonNull HttpClient httpClient,
                                           @NonNull Map<String, String> data,
                                           UserInfo userInfo,
                                           @NonNull String relyingParty,
                                           @NonNull String enroll,
                                           @NonNull String url) throws MalformedURLException, URISyntaxException {
        super(setter);
        this.httpClient = httpClient;
        this.uri = new URL(url).toURI();
        this.data = performData(data, userInfo, relyingParty, enroll);

    }

    private static Setter createSetter(@NonNull String enroll, Integer timeout) {
        Setter setter = Setter.withGroupKey(asKey(String.format("identity-provider-%s", enroll)));
        if (timeout != null) {
            setter.andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
                    .withExecutionTimeoutInMilliseconds(timeout));
        } else {
            setter.andCommandPropertiesDefaults(HystrixCommandProperties.Setter());
        }
        return setter;
    }

    private Map<String, Object> performData(Map<String, String> data, UserInfo userInfo, String relyingParty, String enroll) {
        Map<String, Object> result = createKeyPrefix("data", data);
        result.putAll(createKeyPrefix("userinfo", toMap(userInfo)));
        result.put("relying_party", relyingParty);
        result.put("enroll", enroll);
        return result;
    }

    private Map<String, Object> toMap(UserInfo userInfo) {
        return ofNullable(userInfo)
                .map(UserInfo::toMap)
                .orElse(Collections.emptyMap());
    }

    private static Map<String, Object> createKeyPrefix(String prefix, Map<String, ?> map) {
        return map.entrySet().stream()
                .filter(e -> e.getKey() != null)
                .collect(Collectors.toMap(
                        e -> prefix + "." + e.getKey(),
                        e -> Objects.toString(e.getValue()))
                );
    }

    private static String createAuthHeader(String login, String password) {
        String auth = login + ":" + password;
        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(UTF_8));
        return "Basic " + new String(encodedAuth, UTF_8);
    }

    protected Observable<ReceivedResponse> construct() {
        Promise<ReceivedResponse> promise = httpClient.post(
                uri,
                r -> {
                    r.sslContext(createSSLContext());
                    fillRequestHeaders(r);
                    fillRequestBody(r);
                }
        );
        return RxRatpack.observe(promise);
    }

    private void fillRequestHeaders(RequestSpec requestSpec) throws Exception {
        if (login != null && password != null) {
            requestSpec.headers(headers -> {
                headers.add(HttpHeaders.AUTHORIZATION, createAuthHeader(login, password));

                for (LogHeader logHeader: LogHeader.values()) {
                    if (MDC.get(logHeader.mdcVal()) != null) {
                        headers.add(logHeader.headerVal(), MDC.get(logHeader.mdcVal()));
                    }
                }
            });
        }
    }

    private void fillRequestBody(RequestSpec requestSpec) throws Exception {
        requestSpec.body(body -> {
            body.type(MediaType.APPLICATION_JSON);
            body.text(data.entrySet().stream()
                    .filter(e -> e.getKey() != null && e.getValue() != null)
                    .map(e -> e.getKey() + "=" + encode(e.getValue()))
                    .collect(Collectors.joining("&")));
        });
    }

    @SneakyThrows(UnsupportedEncodingException.class)
    private static String encode(Object value) {
        return URLEncoder.encode(String.valueOf(value), "UTF-8");
    }

    @SneakyThrows
    private SSLContext createSSLContext() {
        SSLContext sslContext = SSLContext.getInstance("SSL");

        sslContext.init(null, new TrustManager[]{new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(X509Certificate[] certs,
                                           String authType) {
            }

            public void checkServerTrusted(X509Certificate[] certs,
                                           String authType) {
            }

        }}, new SecureRandom());
        return sslContext;
    }
}
