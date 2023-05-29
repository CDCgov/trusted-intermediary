package gov.hhs.cdc.trustedintermediary.auth;

import gov.hhs.cdc.trustedintermediary.organizations.OrganizationsSettings;
import gov.hhs.cdc.trustedintermediary.wrappers.AuthEngine;
import gov.hhs.cdc.trustedintermediary.wrappers.Cache;
import gov.hhs.cdc.trustedintermediary.wrappers.InvalidTokenException;
import gov.hhs.cdc.trustedintermediary.wrappers.SecretRetrievalException;
import gov.hhs.cdc.trustedintermediary.wrappers.Secrets;
import gov.hhs.cdc.trustedintermediary.wrappers.TokenGenerationException;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.Formatter;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.FormatterProcessingException;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.TypeReference;
import java.util.Map;
import javax.inject.Inject;

/** TODO */
public class RequestSessionTokenUsecase {

    private static final RequestSessionTokenUsecase INSTANCE = new RequestSessionTokenUsecase();

    private static final String OUR_NAME = "cdc-trusted-intermediary";
    private static final String RS_NAME = "report-stream";
    private static final int TOKEN_TTL = 300;

    @Inject private AuthEngine auth;
    @Inject private Formatter formatter;
    @Inject private Secrets secrets;
    @Inject private OrganizationsSettings organizationsSettings;
    @Inject private Cache cache;

    public static RequestSessionTokenUsecase getInstance() {
        return INSTANCE;
    }

    private RequestSessionTokenUsecase() {}

    public String getToken(AuthRequest request)
            throws InvalidTokenException, IllegalArgumentException, TokenGenerationException,
                    SecretRetrievalException, UnknownOrganizationException {

        var organizationName = request.scope();

        var organization =
                organizationsSettings
                        .findOrganization(request.scope())
                        .orElseThrow(
                                () ->
                                        new UnknownOrganizationException(
                                                "The organization "
                                                        + organizationName
                                                        + " is unknown"));

        // At this point, only organizations registered with us will proceed

        // Validate the JWT is signed by a trusted entity
        var organizationPublicKey = retrieveOrganizationPublicKey(organizationName);
        auth.validateToken(request.jwt(), organizationPublicKey);

        // Provide a short-lived access token for subsequent calls to the TI service
        return auth.generateToken(
                OUR_NAME, OUR_NAME, RS_NAME, RS_NAME, TOKEN_TTL, retrieveOrganizationPublicKey());
    }

    /** TODO: Consolidate; copied from ReportStreamLabOrderSender */
    protected String retrieveOrganizationPublicKey(String organizationName)
            throws SecretRetrievalException {
        var organizationPublicKeyName = "organization-" + organizationName + "-public-key";

        String key = cache.get(organizationPublicKeyName);
        if (key != null) {
            return key;
        }

        key = secrets.getKey(organizationPublicKeyName);
        cache.put(organizationPublicKeyName, key);

        return key;
    }

    /** TODO: Consolidate; copied from ReportStreamLabOrderSender */
    protected String extractToken(String responseBody) throws FormatterProcessingException {
        var value =
                formatter.convertJsonToObject(
                        responseBody, new TypeReference<Map<String, String>>() {});
        return value.get("access_token");
    }
}
