package gov.hhs.cdc.trustedintermediary.wrappers;

import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import java.security.PrivateKey;
import java.util.Date;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;

public class JjwtEngine implements AuthEngine {

    private Jwt jwt;

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
            @NotNull PrivateKey privateKey,
            @NotNull String keyId,
            int expirationSecondsfromNow) {

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
                        .signWith(privateKey);
        String jws = jwsObj.compact();

        return jws;
    }

    @Override
    public boolean isValidToken() {
        return false;
    }

    public Jwt getJwt() {
        return this.jwt;
    }
}
