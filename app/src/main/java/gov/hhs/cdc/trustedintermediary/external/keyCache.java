package gov.hhs.cdc.trustedintermediary.external;

import java.util.HashMap;
import java.util.Map;

public class keyCache {

    private Map<String, String> keys;

    private keyCache() {
        keys = new HashMap<>();
    }

    public keyCache getInstance() {
        return new keyCache();
    }

    void put(String key, String value) {
        keys.put(key, value);
    }

    public String get(String key) {
        return keys.get(key);
    }
}
