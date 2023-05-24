package gov.hhs.cdc.trustedintermediary.etor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Stores keys from Azure to optimize performance. */
public class KeyCache {

    private Map<String, String> keys;

    private KeyCache() {
        // ConcurrentHashMap<>() over Collections.synchronizedMap() due to performance.
        // Synchronized map locks the whole object when reading or writing.
        // Concurrent hashmap locks happen at the bucket level, leaving the read
        // function unlocked when writing.
        keys = new ConcurrentHashMap<>();
    }

    public static KeyCache getInstance() {
        return new KeyCache();
    }

    public void put(String key, String value) {
        keys.put(key, value);
    }

    public String get(String key) {
        return keys.get(key);
    }
}
