package ru.ratauth.interaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.ratauth.utils.StringUtils;

import java.util.Map;
import java.util.StringJoiner;

import static ru.ratauth.utils.URIUtils.appendQuery;

/**
 * @author mgorelikov
 * @since 29/01/16
 */
//TODO Classes like this could be moved from domain to server project
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegistrationResponse {
    private String redirectUrl;
    private Map<String, Object> data;
    private String code;

    public String buildURL() {
        StringJoiner joiner = new StringJoiner("&");
        if (!StringUtils.isBlank(code)) {
            joiner.add("code=" + code);
        }
        if (data != null && !data.isEmpty()) {
            data.entrySet().forEach(entry -> joiner.add(entry.getKey() + "=" + entry.getValue().toString()));
        }
        return appendQuery(redirectUrl, joiner.toString());
    }
}
