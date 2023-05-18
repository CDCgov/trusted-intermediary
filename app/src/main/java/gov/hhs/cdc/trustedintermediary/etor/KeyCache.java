package gov.hhs.cdc.trustedintermediary.etor;

import java.util.HashMap;
import java.util.Map;

public class KeyCache {

    private Map<String, String> keys;

    private KeyCache() {
        keys = new HashMap<>();
    }

    public static KeyCache getInstance() {
        return new KeyCache();
    }

    void put(String key, String value) {
        keys.put(key, value);
    }

    public String get(String key) {
        return keys.get(key);
    }
}
