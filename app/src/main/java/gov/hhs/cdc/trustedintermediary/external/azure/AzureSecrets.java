package gov.hhs.cdc.trustedintermediary.external.azure;

import gov.hhs.cdc.trustedintermediary.wrappers.KeyVault;
import gov.hhs.cdc.trustedintermediary.wrappers.Secrets;
import javax.inject.Inject;

/**
 * This Class implements the Secret interface, and it is used to retrieve azure environment secrets.
 */
public class AzureSecrets implements Secrets {

    private static final AzureSecrets INSTANCE = new AzureSecrets();

    private AzureSecrets() {}

    @Inject private static final KeyVault keyVault;

    public static AzureSecrets getInstance() {
        return INSTANCE;
    }

    @Override
    public String getKey() {

        return keyVault.getKey("senderKey");
    }
}
