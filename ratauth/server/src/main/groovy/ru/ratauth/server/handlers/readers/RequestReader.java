package ru.ratauth.server.handlers.readers;

import java.util.HashMap;
import java.util.Map;

public class RequestReader {

    private final HashMap<String, String> valueMap;

    public RequestReader(Map<String, String> valueMap) {
        this.valueMap = new HashMap<>(valueMap);
    }

    public String extractField(String key, boolean required) {
        return RequestUtil.extractField(valueMap, key, required);
    }

    public String removeField(String key, boolean required) {
        return RequestUtil.removeField(valueMap, key, required);
    }

    public Map<String, String> toMap() {
        return new HashMap<>(valueMap);
    }

}
