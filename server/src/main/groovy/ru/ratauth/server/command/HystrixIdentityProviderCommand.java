package ru.ratauth.server.command;

import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixObservableCommand;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import ratpack.exec.Promise;
import ratpack.http.MediaType;
import ratpack.http.client.HttpClient;
import ratpack.http.client.ReceivedResponse;
import ratpack.rx.RxRatpack;
import ru.ratauth.entities.UserInfo;
import rx.Observable;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.netflix.hystrix.HystrixCommandGroupKey.Factory.asKey;
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
        Map<String, Object> result = new HashMap<>();
        result.put("data", new JSONObject(data));
        result.put("userInfo", new JSONObject(toMap(userInfo)));
        result.put("relying_party", relyingParty);
        result.put("enroll", enroll);
        return result;
    }

    private Map<String, Object> toMap(UserInfo userInfo) {
        return ofNullable(userInfo)
                .map(UserInfo::toMap)
                .orElse(Collections.emptyMap());
    }


    @SneakyThrows(UnsupportedEncodingException.class)
    private static String createAuthHeader(String login, String password) {
        String auth = login + ":" + password;
        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(Charset.forName("UTF-8")));
        return "Basic " + new String(encodedAuth, "UTF-8");
    }

    protected Observable<ReceivedResponse> construct() {
        Promise<ReceivedResponse> promise = httpClient.post(
                uri,
                r -> {
                    r.sslContext(createSSLContext());
                    if (login != null && password != null) {
                        r.headers(headers -> {
                            headers.add(HttpHeaders.AUTHORIZATION, createAuthHeader(login, password));
                            if (MDC.get("session_id") != null) {
                                headers.add("Session-Id", MDC.get("session_id"));
                            }
                            if (MDC.get("user_id") != null) {
                                headers.add("User-Id", MDC.get("user_id"));
                            }
                            if (MDC.get("client_id") != null) {
                                headers.add("Client-Id", MDC.get("client_id"));
                            }
                            if (MDC.get("device_id") != null) {
                                headers.add("Device-Id", MDC.get("device_id"));
                            }
                            if (MDC.get("client_ip") != null) {
                                headers.add("Client-Ip", MDC.get("client_ip"));
                            }
                            if (MDC.get("trace_id") != null) {
                                headers.add("Trace-Id", MDC.get("trace_id"));
                            }
                        });
                    }
                    r.body(body -> {
                        body.type(MediaType.APPLICATION_JSON);
                        body.text(new JSONObject(data).toJSONString());
                        log.info("Request to idp, body is {}", new JSONObject(data).toJSONString());
                    });
                }
        );
        return RxRatpack.observe(promise);
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
