package ru.ratauth.server.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import ru.ratauth.entities.AcrValues;

import java.util.Collections;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "auth.session")
public class SessionConfiguration {

    private Boolean needToCheckSession;

    private List<String> possibleRequiredAcrValues;

    public boolean isNeedToCheckSession() {
        return needToCheckSession == null || needToCheckSession;
    }

    public boolean containsRequiredAcrValues(AcrValues receivedAcrValues) {
        return possibleRequiredAcrValues == null || possibleRequiredAcrValues.isEmpty()
            || !Collections.disjoint(possibleRequiredAcrValues, receivedAcrValues.getValues());
    }

}
