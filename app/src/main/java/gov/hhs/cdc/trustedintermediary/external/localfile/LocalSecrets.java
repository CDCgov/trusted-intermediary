package gov.hhs.cdc.trustedintermediary.external.localfile;
/**
 * This Class implements the Secret interface, and it's purpose is to retrieve secrets from our
 * local environment
 */
import gov.hhs.cdc.trustedintermediary.wrappers.Secrets;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class LocalSecrets implements Secrets {

    private static final LocalSecrets INSTANCE = new LocalSecrets();

    private LocalSecrets() {}

    public static LocalSecrets getInstance() {
        return INSTANCE;
    }

    @Override
    public String getKey() { // What key, sender: TI, client or receiver:TI, client?

        String key = "";

        try {
            key =
                    new String(
                            Files.readAllBytes(
                                    Path.of(
                                            "..",
                                            "mock_credentials",
                                            "my-rsa-local-private-key.pem")));
        } catch (IOException e) {
            // TODO exception handling
        }

        return key;
    }
}
