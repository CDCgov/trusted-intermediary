package gov.hhs.cdc.trustedintermediary.etor.utils.security;

import java.security.NoSuchAlgorithmException;

public interface SecureHash {
    String generateHash(Object hash) throws NoSuchAlgorithmException;
}
