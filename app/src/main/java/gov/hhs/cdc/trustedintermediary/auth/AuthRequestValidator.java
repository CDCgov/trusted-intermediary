package gov.hhs.cdc.trustedintermediary.auth;

import gov.hhs.cdc.trustedintermediary.context.ApplicationContext;
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainRequest;
import gov.hhs.cdc.trustedintermediary.wrappers.AuthEngine;
import gov.hhs.cdc.trustedintermediary.wrappers.Cache;
import gov.hhs.cdc.trustedintermediary.wrappers.InvalidTokenException;
import gov.hhs.cdc.trustedintermediary.wrappers.SecretRetrievalException;
import gov.hhs.cdc.trustedintermediary.wrappers.Secrets;
import java.util.Optional;
import javax.inject.Inject;

/**
 * This class is used to check the validity of a http request. It has methods that extract the
 * bearer token, check is if the token is empty or null, and if the token is valid. For example,
 * expired tokens or tokens not signed by our private key, will be invalid.
 */
public class AuthRequestValidator {

    private static final AuthRequestValidator INSTANCE = new AuthRequestValidator();

    @Inject private AuthEngine jwtEngine;
    @Inject private Cache keyCache;
    @Inject private Secrets secrets;

    private AuthRequestValidator() {}

    public static AuthRequestValidator getInstance() {
        return INSTANCE;
    }

    public boolean isValidAuthenticatedRequest(DomainRequest request)
            throws SecretRetrievalException, IllegalArgumentException {

        var token = extractToken(request);

        if (!tokenHasContent(token)) {
            return false;
        }

        try {
            jwtEngine.validateToken(token, retrievePrivateKey());
            return true;
        } catch (InvalidTokenException e) {
            return false;
        }
    }

    protected String retrievePrivateKey() throws SecretRetrievalException {
        var senderPrivateKey =
                "trusted-intermediary-private-key-" + ApplicationContext.getEnvironment();
        String key = this.keyCache.get(senderPrivateKey);
        if (key != null) {
            return key;
        }

        key = secrets.getKey(senderPrivateKey);
        this.keyCache.put(senderPrivateKey, key);
        return key;
    }

    protected String extractToken(DomainRequest request) {
        var authHeader = Optional.ofNullable(request.getHeaders().get("Authorization")).orElse("");
        return authHeader.replace("Bearer ", "");
    }

    protected boolean tokenHasContent(String token) {
        return token != null && !token.isEmpty();
    }
}
