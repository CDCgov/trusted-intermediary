package gov.hhs.cdc.trustedintermediary.external.localfile;

import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import gov.hhs.cdc.trustedintermediary.wrappers.SecretRetrievalException;
import gov.hhs.cdc.trustedintermediary.wrappers.Secrets;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.inject.Inject;

/**
 * This Class implements the Secret interface, and it's purpose is to retrieve secrets from our
 * local environment
 */
public class LocalSecrets implements Secrets {

    private static final LocalSecrets INSTANCE = new LocalSecrets();

    private LocalSecrets() {}

    @Inject private Logger logger;

    public static LocalSecrets getInstance() {
        return INSTANCE;
    }

    @Override
    public String getKey(String secretName) throws SecretRetrievalException {

        logger.logInfo("Acquiring local key...");
        String key = "";

        try {
            key = Files.readString(Path.of("..", "mock_credentials", secretName + ".pem"));
        } catch (IOException e) {
            var message = "Error getting local key";
            logger.logError(message, e);
            throw new SecretRetrievalException(message, e);
        }

        return key;
    }
}
