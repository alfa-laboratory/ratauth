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
import ru.ratauth.server.extended.common.IDPRequest;
import ru.ratauth.server.services.log.LogHeader;
import rx.Observable;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Map;

import static com.netflix.hystrix.HystrixCommandGroupKey.Factory.asKey;
import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
public class HystrixIdentityProviderCommand extends HystrixObservableCommand<ReceivedResponse> {

    private final HttpClient httpClient;
    private final IDPRequest data;
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
        this.data = new IDPRequest(data, userInfo, relyingParty, enroll);

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

                for (LogHeader logHeader : LogHeader.values()) {
                    if (MDC.get(logHeader.mdcVal()) != null) {
                        headers.add(logHeader.headerVal(), MDC.get(logHeader.mdcVal()));
                    }
                }
            });
        }
    }

    private void fillRequestBody(RequestSpec requestSpec) {
        requestSpec.getBody().type(MediaType.APPLICATION_JSON);
        requestSpec.getBody().text(data.toJsonString());
        log.info("Request to idp, body is {}", data.toJsonString());
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
