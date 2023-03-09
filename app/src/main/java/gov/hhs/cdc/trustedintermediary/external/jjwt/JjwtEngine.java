package gov.hhs.cdc.trustedintermediary.external.jjwt;

import gov.hhs.cdc.trustedintermediary.wrappers.AuthEngine;
import io.jsonwebtoken.*;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;

public class JjwtEngine implements AuthEngine {

    private static final JjwtEngine INSTANCE = new JjwtEngine();

    private JjwtEngine() {}

    public static JjwtEngine getInstance() {
        return INSTANCE;
    }

    @Override
    @NotNull
    public String generateSenderToken(
            @NotNull String sender,
            @NotNull String baseUrl,
            @NotNull String pemKey,
            @NotNull String keyId,
            int expirationSecondsFromNow)
            throws InvalidKeySpecException, NoSuchAlgorithmException {

        JwtBuilder jwsObj =
                Jwts.builder()
                        .setHeaderParam("kid", keyId)
                        .setHeaderParam("typ", "JWT")
                        .setIssuer(sender)
                        .setSubject(sender)
                        .setAudience(baseUrl)
                        .setExpiration(
                                new Date(
                                        System.currentTimeMillis()
                                                + (long) (expirationSecondsFromNow * 1000L)))
                        .setId(UUID.randomUUID().toString())
                        .signWith(readPrivateKey(pemKey));

        return jwsObj.compact();
    }

    private RSAPrivateKey readPrivateKey(@NotNull String pemKey)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        String privatePemKey =
                pemKey.replace("-----BEGIN PRIVATE KEY-----", "")
                        .replaceAll(System.lineSeparator(), "")
                        .replace("-----END PRIVATE KEY-----", "");
        byte[] encode = Base64.getDecoder().decode(privatePemKey);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encode);
        return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
    }
}
