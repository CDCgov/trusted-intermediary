package gov.hhs.cdc.trustedintermediary.external.localfile;

import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import gov.hhs.cdc.trustedintermediary.wrappers.SecretRetrievalException;
import gov.hhs.cdc.trustedintermediary.wrappers.Secrets;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
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

        logger.logInfo("Acquiring local key " + secretName);
        String key = "";

        try {
            key = readSecretFromFileSystem(secretName);
        } catch (SecretRetrievalException exception) {
            logger.logWarning(
                    "Not finding the " + secretName + " on the filesystem, searching in resources");
            key = readSecretFromResources(secretName);
        }

        logger.logInfo("Successfully got local key " + secretName);
        return key;
    }

    private String readSecretFromFileSystem(String secretName) throws SecretRetrievalException {
        try {
            return Files.readString(Path.of("..", "mock_credentials", secretName + ".pem"));
        } catch (IOException exception) {
            throw new SecretRetrievalException(
                    "Error getting local key " + secretName + " from the filesystem", exception);
        }
    }

    private String readSecretFromResources(String secretName) throws SecretRetrievalException {
        try (InputStream secretStream =
                getClass().getClassLoader().getResourceAsStream(secretName + ".pem")) {
            return new String(secretStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception exception) {
            throw new SecretRetrievalException(
                    "Error getting local key " + secretName + " from the resources", exception);
        }
    }
}
