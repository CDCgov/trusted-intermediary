package gov.hhs.cdc.trustedintermediary.external.jjwt;

import gov.hhs.cdc.trustedintermediary.wrappers.AuthEngine;
import gov.hhs.cdc.trustedintermediary.wrappers.TokenGenerationException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;
import javax.annotation.Nonnull;

/**
 * This class implements the AuthEngine and is a "humble object" for the Jjwt library. It's main
 * purpose is to deal with all jwt related transactions such as creating a jwt (json web token).
 */
public class JjwtEngine implements AuthEngine {

    private static final JjwtEngine INSTANCE = new JjwtEngine();

    private JjwtEngine() {}

    public static JjwtEngine getInstance() {
        return INSTANCE;
    }

    @Override
    @Nonnull
    public String generateSenderToken(
            @Nonnull String sender,
            @Nonnull String baseUrl,
            @Nonnull String pemKey,
            @Nonnull String keyId,
            int expirationSecondsFromNow)
            throws TokenGenerationException {

        RSAPrivateKey privateKey;
        try {
            privateKey = readPrivateKey(pemKey);
        } catch (NoSuchAlgorithmException e) {
            throw new TokenGenerationException("The private key algorithm isn't supported", e);
        } catch (Exception e) {
            throw new TokenGenerationException("The private key wasn't formatted correctly", e);
        }

        JwtBuilder jwsObj = null;
        try {
            jwsObj =
                    Jwts.builder()
                            .setHeaderParam("kid", keyId)
                            .setHeaderParam("typ", "JWT")
                            .setIssuer(sender)
                            .setSubject(sender)
                            .setAudience(baseUrl)
                            .setExpiration(
                                    new Date(
                                            System.currentTimeMillis()
                                                    + (expirationSecondsFromNow * 1000L)))
                            .setId(UUID.randomUUID().toString())
                            .signWith(privateKey);

            return jwsObj.compact();
        } catch (JwtException exception) {
            throw new TokenGenerationException(
                    "Jjwt was unable to create or sign the JWT", exception);
        }
    }

    protected RSAPrivateKey readPrivateKey(@Nonnull String pemKey)
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

    @Override
    public LocalDateTime getExpirationDate(String jwt) {

        var tokenOnly = jwt.substring(0, jwt.lastIndexOf('.') + 1);
        Claims claims = Jwts.parserBuilder().build().parseClaimsJwt(tokenOnly).getBody();
        Date expirationDate = claims.getExpiration();

        return LocalDateTime.ofInstant(expirationDate.toInstant(), ZoneId.systemDefault());
    }
}
