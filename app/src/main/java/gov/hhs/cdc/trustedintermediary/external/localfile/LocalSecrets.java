package gov.hhs.cdc.trustedintermediary.external.localfile;
/**
 * This Class implements the Secret interface, and it's purpose is to retrieve secrets from our
 * local environment
 */
import gov.hhs.cdc.trustedintermediary.wrappers.Secrets;

public class LocalSecrets implements Secrets {

    private static final LocalSecrets INSTANCE = new LocalSecrets();

    private LocalSecrets() {}

    public static LocalSecrets getInstance() {
        return INSTANCE;
    }

    @Override
    public String getKey() {
        return null;
    }
}
