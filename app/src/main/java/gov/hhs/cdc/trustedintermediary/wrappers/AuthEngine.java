package gov.hhs.cdc.trustedintermediary.wrappers;
/**
 * This interface provides a blueprint for all auth. related transactions. For example,
 * generateSenderToken() generates a token using ETOR's private key.
 */
import org.jetbrains.annotations.NotNull;

public interface AuthEngine {
    @NotNull
    String generateSenderToken(
            @NotNull String sender,
            @NotNull String baseUrl,
            @NotNull String pemKey,
            @NotNull String keyId,
            int expirationSecondsFromNow)
            throws Exception; // TODO dedicated exception instead of generic
}
