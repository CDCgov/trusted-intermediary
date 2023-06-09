package gov.hhs.cdc.trustedintermediary.external.jjwt;

import gov.hhs.cdc.trustedintermediary.wrappers.AuthEngine;
import gov.hhs.cdc.trustedintermediary.wrappers.InvalidTokenException;
import gov.hhs.cdc.trustedintermediary.wrappers.TokenGenerationException;
import io.jsonwebtoken.ClaimJwtException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
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
    public String generateToken(
            String keyId,
            String issuer,
            String subject,
            String audience,
            int expirationSecondsFromNow,
            String pemKey)
            throws TokenGenerationException {

        PrivateKey privateKey;
        try {
            privateKey = readPrivateKey(pemKey);
        } catch (NoSuchAlgorithmException e) {
            throw new TokenGenerationException("The private key algorithm isn't supported", e);
        } catch (Exception e) {
            throw new TokenGenerationException("The private key wasn't formatted correctly", e);
        }

        JwtBuilder jwsObj;
        try {
            jwsObj =
                    Jwts.builder()
                            .setHeaderParam("kid", keyId)
                            .setHeaderParam("typ", "JWT")
                            .setIssuer(issuer)
                            .setSubject(subject)
                            .setAudience(audience)
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

    @Override
    public LocalDateTime getExpirationDate(String jwt) {

        var tokenOnly = jwt.substring(0, jwt.lastIndexOf('.') + 1);

        Claims claims;
        try {
            claims = Jwts.parserBuilder().build().parseClaimsJwt(tokenOnly).getBody();
        } catch (ClaimJwtException e) {
            claims = e.getClaims();
        }

        Date expirationDate = claims.getExpiration();
        return LocalDateTime.ofInstant(expirationDate.toInstant(), ZoneId.systemDefault());
    }

    @Override
    public void validateToken(String jwt, String encodedKey)
            throws InvalidTokenException, IllegalArgumentException {

        try {
            byte[] encode = Base64.getDecoder().decode(stripPemKeyHeaderAndFooter(encodedKey));
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encode);
            var key = keyFactory.generatePublic(keySpec);
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(jwt);

        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidTokenException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("The key algorithm isn't supported", e);
        } catch (Exception e) {
            throw new IllegalArgumentException("The key wasn't formatted correctly", e);
        }
    }

    @Override
    public boolean isValidAccessToken(String jwt, String privateKey) {

        try {
            Jws<Claims> claimsJws =
                    Jwts.parserBuilder()
                            .setSigningKey(readPrivateKey(privateKey))
                            .build()
                            .parseClaimsJws(jwt);

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    protected PrivateKey readPrivateKey(@Nonnull String pemKey)
            throws NoSuchAlgorithmException, InvalidKeySpecException {

        byte[] encode = Base64.getDecoder().decode(stripPemKeyHeaderAndFooter(pemKey));

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encode);
        return keyFactory.generatePrivate(keySpec);
    }

    private String stripPemKeyHeaderAndFooter(String pemKey) {
        return pemKey.replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll(System.lineSeparator(), "");
    }
}
