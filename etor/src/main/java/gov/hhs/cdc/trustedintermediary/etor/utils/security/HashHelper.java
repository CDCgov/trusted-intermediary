package gov.hhs.cdc.trustedintermediary.etor.utils.security;

import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.inject.Inject;
import org.apache.commons.codec.binary.Hex;

public class HashHelper implements SecureHash {
    @Inject Logger logger;

    public <T> String generateHash(T input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            byte[] objBytes = input.toString().getBytes(StandardCharsets.UTF_8);
            byte[] hashBytes = digest.digest(objBytes);
            return Hex.encodeHexString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            logger.logError("Algorithm does not exist!", e);
            throw new RuntimeException(e);
        }
    }
}
