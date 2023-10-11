package gov.hhs.cdc.trustedintermediary.auth;

import gov.hhs.cdc.trustedintermediary.context.ApplicationContext;
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainRequest;
import gov.hhs.cdc.trustedintermediary.wrappers.AuthEngine;
import gov.hhs.cdc.trustedintermediary.wrappers.Cache;
import gov.hhs.cdc.trustedintermediary.wrappers.InvalidTokenException;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import gov.hhs.cdc.trustedintermediary.wrappers.SecretRetrievalException;
import gov.hhs.cdc.trustedintermediary.wrappers.Secrets;
import java.util.Optional;
import javax.inject.Inject;

/**
 * This class is used to check the validity of a http request. It has methods that extract the
 * bearer token, check if the token is empty or null, and if the token is valid. For example,
 * expired tokens, empty tokens, or tokens not signed by our private key, will be invalid.
 */
public class AuthRequestValidator {

    private static final AuthRequestValidator INSTANCE = new AuthRequestValidator();

    @Inject private AuthEngine jwtEngine;
    @Inject private Cache keyCache;
    @Inject private Secrets secrets;
    @Inject private Logger logger;

    private AuthRequestValidator() {}

    public static AuthRequestValidator getInstance() {
        return INSTANCE;
    }

    public boolean isValidAuthenticatedRequest(DomainRequest request)
            throws SecretRetrievalException, IllegalArgumentException {

        logger.logInfo("Authenticating request...");
        var token = extractToken(request);

        if (!tokenHasContent(token)) {
            logger.logError("Invalid token, token is empty or null!");
            return false;
        }

        try {
            logger.logDebug("Checking if bearer token is valid...");
            jwtEngine.validateToken(token, retrievePublicKey());
            logger.logInfo("Bearer token is valid");
            return true;
        } catch (InvalidTokenException e) {
            logger.logError("Invalid bearer token!", e);
            return false;
        }
    }

    protected String retrievePublicKey() throws SecretRetrievalException {
        var ourPublicKey = "trusted-intermediary-public-key-" + ApplicationContext.getEnvironment();
        String key = this.keyCache.get(ourPublicKey);
        if (key != null) {
            return key;
        }

        key = secrets.getKey(ourPublicKey);
        this.keyCache.put(ourPublicKey, key);
        return key;
    }

    protected String extractToken(DomainRequest request) {
        logger.logDebug("Extracting token from request...");
        var authHeader = Optional.ofNullable(request.getHeaders().get("Authorization")).orElse("");
        return authHeader.replace("Bearer ", "");
    }

    protected boolean tokenHasContent(String token) {
        return token != null && !token.isEmpty();
    }
}
