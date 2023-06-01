package gov.hhs.cdc.trustedintermediary.auth;

import gov.hhs.cdc.trustedintermediary.context.ApplicationContext;
import gov.hhs.cdc.trustedintermediary.organizations.OrganizationsSettings;
import gov.hhs.cdc.trustedintermediary.wrappers.AuthEngine;
import gov.hhs.cdc.trustedintermediary.wrappers.Cache;
import gov.hhs.cdc.trustedintermediary.wrappers.InvalidTokenException;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import gov.hhs.cdc.trustedintermediary.wrappers.SecretRetrievalException;
import gov.hhs.cdc.trustedintermediary.wrappers.Secrets;
import gov.hhs.cdc.trustedintermediary.wrappers.TokenGenerationException;
import javax.inject.Inject;

/**
 * Checks the token passed in by a client trying to log in. If the token passes muster, we generate
 * a token of our own to be used as an API key by the client in subsequent calls.
 */
public class RequestSessionTokenUsecase {

    private static final RequestSessionTokenUsecase INSTANCE = new RequestSessionTokenUsecase();

    private static final String OUR_NAME = "cdc-trusted-intermediary";
    private static final int TOKEN_TTL = 300;

    @Inject private AuthEngine auth;
    @Inject private Secrets secrets;
    @Inject private OrganizationsSettings organizationsSettings;
    @Inject private Cache cache;
    @Inject private Logger logger;

    public static RequestSessionTokenUsecase getInstance() {
        return INSTANCE;
    }

    private RequestSessionTokenUsecase() {}

    public String getToken(AuthRequest request)
            throws InvalidTokenException, IllegalArgumentException, TokenGenerationException,
                    SecretRetrievalException, UnknownOrganizationException {

        logger.logInfo("Validating that organization {} exists", request.scope());
        var organization =
                organizationsSettings
                        .findOrganization(request.scope())
                        .orElseThrow(
                                () ->
                                        new UnknownOrganizationException(
                                                "The organization "
                                                        + request.scope()
                                                        + " is unknown"));

        // At this point, only organizations registered with us will proceed
        logger.logInfo("Organization {} exists, validating their token", organization.getName());

        // Validate the JWT is signed by a trusted entity
        var organizationPublicKey = retrieveOrganizationPublicKey(organization.getName());
        auth.validateToken(request.jwt(), organizationPublicKey);

        logger.logInfo("Organization {} token is valid", organization.getName());

        // Provide a short-lived access token for subsequent calls to the TI service
        logger.logInfo("Generating TI login token");
        var tiPrivateKey = retrieveTiPrivateKey();
        return auth.generateToken(
                OUR_NAME,
                OUR_NAME,
                organization.getName(),
                organization.getName(),
                TOKEN_TTL,
                tiPrivateKey);
    }

    protected String retrieveOrganizationPublicKey(String organizationName)
            throws SecretRetrievalException {
        return retrieveKey("organization-" + organizationName + "-public-key");
    }

    protected String retrieveTiPrivateKey() throws SecretRetrievalException {
        return retrieveKey(
                "report-stream-sender-private-key-" + ApplicationContext.getEnvironment());
    }

    private String retrieveKey(String keyId) throws SecretRetrievalException {
        String key = cache.get(keyId);
        if (key != null) {
            return key;
        }

        key = secrets.getKey(keyId);
        cache.put(keyId, key);

        return key;
    }
}
