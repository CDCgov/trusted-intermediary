package gov.hhs.cdc.trustedintermediary.external.localfile;

import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import gov.hhs.cdc.trustedintermediary.wrappers.SecretRetrievalException;
import gov.hhs.cdc.trustedintermediary.wrappers.Secrets;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
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

        key = readSecretFromResources(secretName);

        logger.logInfo("Successfully got local key " + secretName);
        return key;
    }

    protected String readSecretFromResources(String secretName) throws SecretRetrievalException {
        try (InputStream secretStream =
                getClass().getClassLoader().getResourceAsStream(secretName + ".pem")) {
            return new String(secretStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception exception) {
            throw new SecretRetrievalException(
                    "Error getting local key " + secretName + " from the resources", exception);
        }
    }
}
