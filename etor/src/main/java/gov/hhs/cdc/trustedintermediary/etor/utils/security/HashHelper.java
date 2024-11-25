package gov.hhs.cdc.trustedintermediary.etor.utils.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public class HashHelper {

    private static final HashHelper INSTANCE = new HashHelper();

    public static HashHelper getInstance() {
        return INSTANCE;
    }

    public String generateHash(Object input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA3-512");
            byte[] objBytes = input.toString().getBytes(StandardCharsets.UTF_8);
            byte[] hashBytes = digest.digest(objBytes);
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Algorithm does not exist!", e);
        }
    }
}
