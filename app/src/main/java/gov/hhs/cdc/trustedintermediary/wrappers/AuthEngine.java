package gov.hhs.cdc.trustedintermediary.wrappers;

import org.jetbrains.annotations.NotNull;

public interface AuthEngine {
    @NotNull
    String generateSenderToken(
            @NotNull String sender,
            @NotNull String baseUrl,
            @NotNull String pemKey,
            @NotNull String keyId,
            int expirationSecondsfromNow)
            throws Exception;

    @NotNull
    boolean isValidToken(@NotNull String token, @NotNull String key);
}
