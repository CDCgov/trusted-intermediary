package gov.hhs.cdc.trustedintermediary.external.localfile;
/**
 * This Class implements the Secret interface, and it's purpose is to retrieve secrets from our
 * local environment
 */
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import gov.hhs.cdc.trustedintermediary.wrappers.Secrets;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.inject.Inject;

public class LocalSecrets implements Secrets {

    private static final LocalSecrets INSTANCE = new LocalSecrets();

    private LocalSecrets() {}

    @Inject private Logger logger;

    public static LocalSecrets getInstance() {
        return INSTANCE;
    }

    @Override
    public String getKey(String secretName) {

        logger.logInfo("Acquiring local key...");
        String key = "";

        try {
            key =
                    new String(
                            Files.readAllBytes(
                                    Path.of("..", "mock_credentials", secretName + ".pem")));
        } catch (IOException e) {
            // TODO exception handling
            logger.logError("Error getting local key", e);
        }

        return key;
    }
}
