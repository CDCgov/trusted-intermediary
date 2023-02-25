package gov.hhs.cdc.trustedintermediary.wrappers;

import java.security.PrivateKey;
import org.jetbrains.annotations.NotNull;

public interface AuthEngine {
    @NotNull
    String generateSenderToken(
            @NotNull String sender,
            @NotNull String baseUrl,
            @NotNull PrivateKey privateKey,
            @NotNull String keyId,
            int expirationSecondsfromNow);

    boolean isValidToken();
}
