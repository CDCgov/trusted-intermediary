package gov.hhs.cdc.trustedintermediary.wrappers;

import io.jsonwebtoken.*;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;

public class JjwtEngine implements AuthEngine {

    private static final JjwtEngine INSTANCE = new JjwtEngine();

    private JjwtEngine() {}
    ;

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
            int expirationSecondsfromNow)
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
                                                + (long) (expirationSecondsfromNow * 1000)))
                        .setId(String.valueOf(UUID.randomUUID()))
                        .signWith(readPrivateKey(pemKey));

        return jwsObj.compact();
    }

    @Override
    @NotNull
    public boolean isValidToken(@NotNull String token, @NotNull String key) {

        // Use the publicKey if a privateKey was used to sign the jwt.
        // Use the same secretKey if a secretKey was used to sign the jwt.
        try {
            Jws jwtClaims =
                    Jwts.parserBuilder()
                            .setSigningKey(readPublicKey(key))
                            .build()
                            .parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            // TODO exception handling, invalid token!
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e); // reading key didn't work
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e); // reading key didn't work
        }
        return false;
    }

    private RSAPublicKey readPublicKey(@NotNull String pemKey)
            throws InvalidKeySpecException, NoSuchAlgorithmException {
        String publicPemKey =
                pemKey.replace("-----BEGIN PUBLIC KEY-----", "")
                        .replaceAll(System.lineSeparator(), "")
                        .replace("-----END PUBLIC KEY-----", "");

        byte[] encoded = Base64.getDecoder().decode(publicPemKey);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);
        return (RSAPublicKey) keyFactory.generatePublic(keySpec);
    }

    private RSAPrivateKey readPrivateKey(@NotNull String pemKey)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        String privatePemKey =
                pemKey.replace("-----", "")
                        .replace("BEGIN ", "")
                        .replace("PRIVATE ", "")
                        .replace("KEY", "")
                        .replaceAll(System.lineSeparator(), "")
                        .replace("END ", "")
                        .replace("-----END PRIVATE KEY-----", "");
        byte[] encode = Base64.getDecoder().decode(privatePemKey);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encode);
        return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
    }
}
