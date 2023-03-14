package gov.hhs.cdc.trustedintermediary.external.azure;
/**
 * This Class implements the Secret interface, and it is used to retrieve azure environment secrets.
 */
import gov.hhs.cdc.trustedintermediary.wrappers.Secrets;

public class AzureSecrets implements Secrets {

    private static final AzureSecrets INSTANCE = new AzureSecrets();

    private AzureSecrets() {}

    public static AzureSecrets getInstance() {
        return INSTANCE;
    }

    @Override
    public String getKey() {
        return null;
    }
}
