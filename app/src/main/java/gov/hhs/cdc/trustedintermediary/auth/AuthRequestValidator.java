package gov.hhs.cdc.trustedintermediary.auth;

import gov.hhs.cdc.trustedintermediary.context.ApplicationContext;
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainRequest;
import gov.hhs.cdc.trustedintermediary.wrappers.AuthEngine;
import gov.hhs.cdc.trustedintermediary.wrappers.Cache;
import gov.hhs.cdc.trustedintermediary.wrappers.InvalidTokenException;
import gov.hhs.cdc.trustedintermediary.wrappers.SecretRetrievalException;
import gov.hhs.cdc.trustedintermediary.wrappers.Secrets;
import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;

/** TODO javadocs */
public class AuthRequestValidator {

    private static final AuthRequestValidator INSTANCE = new AuthRequestValidator();

    private Map<String, String> headers;
    @Inject private AuthEngine jwtEngine;
    @Inject private Cache keyCache;
    @Inject private Secrets secrets;
    private String token;

    private AuthRequestValidator() {}

    public static AuthRequestValidator getInstance() {
        return INSTANCE;
    }

    public void init(DomainRequest request) {
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

    public boolean isValidToken() {

        try {
            jwtEngine.validateToken(this.token, retrievePrivateKey());
            return true;
        } catch (SecretRetrievalException | InvalidTokenException e) {
            return false;
        }
    }
}
