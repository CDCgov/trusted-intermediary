package gov.hhs.cdc.trustedintermediary.wrappers;

import javax.annotation.Nonnull;

/**
 * This interface provides a blueprint for all auth. related transactions. For example,
 * generateSenderToken() generates a token using ETOR's private key.
 */
public interface AuthEngine {
    @Nonnull
    String generateSenderToken(
            @Nonnull String sender,
            @Nonnull String baseUrl,
            @Nonnull String pemKey,
            @Nonnull String keyId,
            int expirationSecondsFromNow)
            throws TokenGenerationException;
}
