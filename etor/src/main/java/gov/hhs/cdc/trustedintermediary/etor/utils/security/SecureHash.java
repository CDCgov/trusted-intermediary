package gov.hhs.cdc.trustedintermediary.etor.utils.security;

import java.security.NoSuchAlgorithmException;

public interface SecureHash {
    <T> String generateHash(T hash) throws NoSuchAlgorithmException;
}
