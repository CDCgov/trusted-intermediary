package gov.hhs.cdc.trustedintermediary.etor.demographics;

import gov.hhs.cdc.trustedintermediary.context.ApplicationContext;
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainRequest;
import gov.hhs.cdc.trustedintermediary.external.azure.AzureSecrets;
import gov.hhs.cdc.trustedintermediary.external.inmemory.KeyCache;
import gov.hhs.cdc.trustedintermediary.external.jackson.Jackson;
import gov.hhs.cdc.trustedintermediary.external.jjwt.JjwtEngine;
import gov.hhs.cdc.trustedintermediary.wrappers.AuthEngine;
import gov.hhs.cdc.trustedintermediary.wrappers.Cache;
import gov.hhs.cdc.trustedintermediary.wrappers.SecretRetrievalException;
import gov.hhs.cdc.trustedintermediary.wrappers.Secrets;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.Formatter;
import java.util.Map;
import java.util.Optional;

/** TODO javadocs */
public class DemographicsRequestValidator {

    private Map<String, String> headers;
    private AuthEngine jwtEngine = JjwtEngine.getInstance();

    private Formatter formatter = Jackson.getInstance();
    private Cache keyCache = KeyCache.getInstance();
    private Secrets secrets = AzureSecrets.getInstance();
    private String token;

    public DemographicsRequestValidator(DomainRequest request) {

        this.headers = request.getHeaders();
        this.token = this.extractToken();
    }

    protected String retrievePrivateKey() throws SecretRetrievalException {
        var senderPrivateKey =
                "report-stream-sender-private-key-" + ApplicationContext.getEnvironment();
        String key = this.keyCache.get(senderPrivateKey);
        if (key != null) {
            return key;
        }

        key = secrets.getKey(senderPrivateKey);
        this.keyCache.put(senderPrivateKey, key);
        return key;
    }

    protected String extractToken() {
        Optional<String> optToken = Optional.ofNullable(this.headers.get("Authorization"));

        return optToken.orElse("");
    }

    public boolean isValidToken() throws SecretRetrievalException {

        return jwtEngine.isValidateAccessToken(this.token, retrievePrivateKey());
    }
}
