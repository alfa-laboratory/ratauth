package ru.ratauth.server.command;

import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixObservableCommand;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.validation.constraints.NotNull;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import ratpack.exec.Promise;
import ratpack.http.MediaType;
import ratpack.http.client.HttpClient;
import ratpack.http.client.ReceivedResponse;
import ratpack.rx.RxRatpack;
import ru.ratauth.entities.UserInfo;
import rx.Observable;

import static com.netflix.hystrix.HystrixCommandGroupKey.Factory.asKey;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Optional.ofNullable;

@Slf4j
public class HystrixUpdateServiceCommand extends HystrixObservableCommand<ReceivedResponse> {

    private final HttpClient httpClient;
    private final Map<String, Object> data;
    private final URI uri;

    private String login;
    private String password;

    public HystrixUpdateServiceCommand(
        @NonNull HttpClient httpClient,
        @NonNull Map<String, String> data,
        @NonNull String relyingParty,
        @NotNull String updateService,
        @NonNull String uri,
        String login,
        String password,
        Integer timeout) throws MalformedURLException, URISyntaxException {

        this(createSetter(updateService, timeout), httpClient, data, relyingParty, uri);
        this.login = login;
        this.password = password;
    }

    private HystrixUpdateServiceCommand(
        Setter setter,
        @NonNull HttpClient httpClient,
        @NonNull Map<String, String> data,
        @NonNull String relyingParty,
        @NonNull String url) throws MalformedURLException, URISyntaxException {

        super(setter);
        this.httpClient = httpClient;
        this.uri = new URL(url).toURI();
        this.data = performData(data, relyingParty);
    }

    private static Setter createSetter(@NonNull String updateService, Integer timeout) {
        Setter setter = Setter.withGroupKey(asKey(String.format("update-service-%s", updateService)));
        if (timeout != null) {
            setter.andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
                .withExecutionTimeoutInMilliseconds(timeout));
        } else {
            setter.andCommandPropertiesDefaults(HystrixCommandProperties.Setter());
        }
        return setter;
    }

    private Map<String, Object> performData(Map<String, String> data, String relyingParty) {
        Map<String, Object> result = createKeyPrefix("data", data);
        result.put("relying_party", relyingParty);
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

    @SneakyThrows(UnsupportedEncodingException.class)
    private static String createAuthHeader(String login, String password) {
        String auth = login + ":" + password;
        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(UTF_8));
        return "Basic " + new String(encodedAuth, UTF_8);
    }

    protected Observable<ReceivedResponse> construct() {
        Promise<ReceivedResponse> promise = makePostRequest();
        return RxRatpack.observe(promise);
    }

    private Promise<ReceivedResponse> makePostRequest() {
        return httpClient.post(
            uri,
            r -> {
                r.sslContext(createSSLContext());
                if (login != null && password != null) {
                    r.headers(headers -> headers.add(HttpHeaders.AUTHORIZATION, createAuthHeader(login, password)));
                }
                r.body(body -> {
                    body.type(MediaType.APPLICATION_JSON);
                    body.text(data.entrySet().stream()
                        .filter(e -> e.getKey() != null && e.getValue() != null)
                        .map(e -> e.getKey() + "=" + e.getValue())
                        .collect(Collectors.joining("&")));
                });
            }
        );
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
