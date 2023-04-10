package gov.hhs.cdc.trustedintermediary.external.azure;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import gov.hhs.cdc.trustedintermediary.context.ApplicationContext;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import gov.hhs.cdc.trustedintermediary.wrappers.Secrets;
import javax.inject.Inject;

/**
 * This Class implements the Secret interface, and it is a "humble object" that uses the "Azure
 * Security Key Vault" and "Azure Identity" dependencies; and it is used to retrieve azure
 * environment secrets along-side Azure keys from the Azure key vault.
 */
public class AzureSecrets implements Secrets {

    private final String keyVaultName =
            ApplicationContext.getProperty("KEY_VAULT_NAME"); // Verify the env variable name
    private final String keyVaultUri = "https://" + keyVaultName + ".vault.azure.net";
    private final SecretClient secretClient =
            new SecretClientBuilder()
                    .vaultUrl(keyVaultUri)
                    .credential(
                            new DefaultAzureCredentialBuilder()
                                    .build()) // looks for AZURE_CLIENT_ID, AZURE_CLIENT_SECRET, and
                    // AZURE_TENANT_ID
                    .buildClient();

    private static final AzureSecrets INSTANCE = new AzureSecrets();
    @Inject private Logger logger;

    private AzureSecrets() {}

    public static AzureSecrets getInstance() {
        return INSTANCE;
    }

    @Override
    public String getKey(String secretName) {

        logger.logInfo("Acquiring Azure key...");
        KeyVaultSecret storedSecret = secretClient.getSecret(secretName);
        return storedSecret.getValue();
    }
}
