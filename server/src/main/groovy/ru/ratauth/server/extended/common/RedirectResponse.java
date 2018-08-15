package ru.ratauth.server.extended.common;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;
import ru.ratauth.server.utils.RedirectUtils;

@Data
@AllArgsConstructor
abstract public class RedirectResponse {

    final private String location;

    public abstract String putRedirectParameters(String key, String value);

    public abstract Map<String, String> getRedirectParameters();

    public String getRedirectURL() {
        return RedirectUtils.createRedirectURI(getLocation(), getRedirectParameters());
    }

    @SneakyThrows(UnsupportedEncodingException.class)
    private static String encoded(String source) {
        return URLEncoder.encode(source, "UTF-8");
    }
}
