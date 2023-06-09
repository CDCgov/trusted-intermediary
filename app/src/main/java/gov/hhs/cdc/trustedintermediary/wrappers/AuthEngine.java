package gov.hhs.cdc.trustedintermediary.wrappers;

import java.time.LocalDateTime;

/**
 * This interface provides a blueprint for all auth. related transactions. For example,
 * generateSenderToken() generates a token using ETOR's private key.
 */
public interface AuthEngine {

    String generateToken(
            String keyId,
            String issuer,
            String subject,
            String audience,
            int expirationSecondsFromNow,
            String pemKey)
            throws TokenGenerationException;

    LocalDateTime getExpirationDate(String token);

    void validateToken(String jwt, String publicKey)
            throws InvalidTokenException, IllegalArgumentException;

    boolean isValidAccessToken(String jwt, String privateKey);
}
