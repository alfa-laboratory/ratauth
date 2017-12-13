package ru.ratauth.server.extended.enroll.verify;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;
import ru.ratauth.server.utils.RedirectUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
abstract public class RedirectResponse {

    final private String location;

    abstract String putRedirectParameters(String key, String value);

    abstract Map<String, String> getRedirectParameters();

    public String getRedirectURL() {
        return RedirectUtils.createRedirectURI(getLocation(), getRedirectParameters());
    }

    @SneakyThrows(UnsupportedEncodingException.class)
    private static String encoded(String source) {
        return URLEncoder.encode(source, "UTF-8");
    }
}
