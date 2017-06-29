package ru.ratauth.server.extended.enroll.verify;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
abstract public class RedirectResponse {

    final private String location;

    abstract Map<String, String> getRedirectParameters();

    public String getRedirectURL() {
        return getLocation() + "?" + getRedirectParameters().entrySet().stream()
                .filter(e -> e.getValue() != null)
                .map(e -> e.getKey() + "=" + encoded(e.getValue()))
                .collect(Collectors.joining("&"));
    }

    @SneakyThrows(UnsupportedEncodingException.class)
    private static String encoded(String source) {
        return URLEncoder.encode(source, "UTF-8");
    }
}
