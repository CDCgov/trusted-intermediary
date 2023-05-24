package gov.hhs.cdc.trustedintermediary.wrappers;

public interface Cache {

    void put(String key, String value);

    String get(String key);
}
