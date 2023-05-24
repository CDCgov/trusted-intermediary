package gov.hhs.cdc.trustedintermediary.external.in_memory;

import gov.hhs.cdc.trustedintermediary.wrappers.Cache;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Stores keys from Azure to optimize performance. */
public class KeyCache implements Cache {

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

    @Override
    public void put(String key, String value) {
        keys.put(key, value);
    }

    @Override
    public String get(String key) {
        return keys.get(key);
    }
}
