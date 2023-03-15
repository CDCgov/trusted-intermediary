package gov.hhs.cdc.trustedintermediary.external.azure;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import gov.hhs.cdc.trustedintermediary.context.ApplicationContext;
import gov.hhs.cdc.trustedintermediary.wrappers.KeyVault;

/**
 * This class implements the KeyVault interface, and it is a "humble object" that uses the Azure
 * security key vault and Azure identity dependencies. The main function of this wrapper is to
 * retrieve keys from Azure key vault.
 */
public class AzurekeyVault implements KeyVault {

    private static final AzurekeyVault INSTANCE = new AzurekeyVault();

    private AzurekeyVault() {}

    private final String keyVaultName =
            ApplicationContext.getProperty("KEY_VAULT_NAME"); // Verify the env variable name
    private final String keyVaultUri = "https://" + keyVaultName + ".vault.azure.net";

    // TODO logger

    private final SecretClient secretClient =
            new SecretClientBuilder()
                    .vaultUrl(keyVaultUri)
                    .credential(
                            new DefaultAzureCredentialBuilder()
                                    .build()) // looks for AZURE_CLIENT_ID, AZURE_CLIENT_SECRET, and
                    // AZURE_TENANT_ID
                    .buildClient();

    public static AzurekeyVault getInstance() {
        return INSTANCE;
    }

    @Override
    public String getKey(String secretName) {
        KeyVaultSecret storedSecret = secretClient.getSecret(secretName);
        return storedSecret.getValue();
    }
}
