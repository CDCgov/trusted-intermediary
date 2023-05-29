package gov.hhs.cdc.trustedintermediary.auth;

import gov.hhs.cdc.trustedintermediary.context.ApplicationContext;
import gov.hhs.cdc.trustedintermediary.organizations.OrganizationsSettings;
import gov.hhs.cdc.trustedintermediary.wrappers.AuthEngine;
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
        // TODO get key from Azure and/or cache
        var rsPublicKey =
                "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAlDsjJmbSl1R/9F8HeyJgT5tMVJp7Svk6N80R+LitxwgNqd9SUSaLjTG662MssViR1nPsy2j/ieLVvKPCj51DRW5h5kVcaumEQxacm6MjOUGPYQ0Y1j8dWxWlkNqH1iRowXZH6ABHcwcecWWyf/lCRt12b0I+n5TJ/F8VVzJ7jRAjHkaOLHmM5tUI1dTJZAReh/qlXQmgjl9u2Pn4YazK8zYYnvplTvif+HuoIeR+Cll7w63Ue6/2OJVTOvblYpx7TG9ZHVEZDnoIks/cvRDnZKShLPql9RHDt5JhsVrFCdOdWa4IOw/IdSXWT/+VzmBiJQw9hhV53IPSUVyp/YaN0QIDAQAB"; // pragma: allowlist secret
        auth.validateToken(request.jwt(), rsPublicKey);

        // Provide a short-lived access token for subsequent calls to the TI service
        return auth.generateToken(
                OUR_NAME, OUR_NAME, RS_NAME, RS_NAME, TOKEN_TTL, retrievePrivateKey());
    }

    /** TODO: Consolidate; copied from ReportStreamLabOrderSender */
    protected String retrievePrivateKey() throws SecretRetrievalException {
        var senderPrivateKey =
                "report-stream-sender-private-key-" + ApplicationContext.getEnvironment();
        // String key = getCachedPrivateKey();
        // if (key != null) {
        //     return key;
        // }

        String key = secrets.getKey(senderPrivateKey);
        // setCachedPrivateKey(key);
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
