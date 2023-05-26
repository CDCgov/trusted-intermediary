package gov.hhs.cdc.trustedintermediary.auth;

import gov.hhs.cdc.trustedintermediary.context.ApplicationContext;
import gov.hhs.cdc.trustedintermediary.wrappers.AuthEngine;
import gov.hhs.cdc.trustedintermediary.wrappers.Formatter;
import gov.hhs.cdc.trustedintermediary.wrappers.FormatterProcessingException;
import gov.hhs.cdc.trustedintermediary.wrappers.InvalidTokenException;
import gov.hhs.cdc.trustedintermediary.wrappers.SecretRetrievalException;
import gov.hhs.cdc.trustedintermediary.wrappers.Secrets;
import gov.hhs.cdc.trustedintermediary.wrappers.TokenGenerationException;
import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;

/** TODO */
public class RequestSessionTokenUsecase {

    @Inject private AuthEngine auth;
    @Inject private Formatter jackson;
    @Inject private Secrets secrets;

    private static final RequestSessionTokenUsecase INSTANCE = new RequestSessionTokenUsecase();

    private static final String RS_URL_PREFIX_PROPERTY = "REPORT_STREAM_URL_PREFIX";
    private static final String RS_DOMAIN_NAME =
            Optional.ofNullable(ApplicationContext.getProperty(RS_URL_PREFIX_PROPERTY))
                    .map(urlPrefix -> urlPrefix.replace("https://", "").replace("http://", ""))
                    .orElse("");
    private static final String CLIENT_NAME = "flexion.etor-service-sender";

    public static RequestSessionTokenUsecase getInstance() {
        return INSTANCE;
    }

    private RequestSessionTokenUsecase() {}

    public String getToken(AuthRequest request)
            throws InvalidTokenException, IllegalArgumentException, TokenGenerationException,
                    SecretRetrievalException {

        // Validate the JWT is signed by a trusted entity
        // TODO get key from Azure and/or cache
        var rsPublicKey =
                "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAlDsjJmbSl1R/9F8HeyJgT5tMVJp7Svk6N80R+LitxwgNqd9SUSaLjTG662MssViR1nPsy2j/ieLVvKPCj51DRW5h5kVcaumEQxacm6MjOUGPYQ0Y1j8dWxWlkNqH1iRowXZH6ABHcwcecWWyf/lCRt12b0I+n5TJ/F8VVzJ7jRAjHkaOLHmM5tUI1dTJZAReh/qlXQmgjl9u2Pn4YazK8zYYnvplTvif+HuoIeR+Cll7w63Ue6/2OJVTOvblYpx7TG9ZHVEZDnoIks/cvRDnZKShLPql9RHDt5JhsVrFCdOdWa4IOw/IdSXWT/+VzmBiJQw9hhV53IPSUVyp/YaN0QIDAQAB"; // pragma: allowlist secret
        auth.validateToken(request.jwt(), rsPublicKey);

        // Provide a short-lived access token for subsequent calls to the TI service
        return auth.generateSenderToken(
                CLIENT_NAME, RS_DOMAIN_NAME, retrievePrivateKey(), CLIENT_NAME, 300);
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

        Map<String, String> value;

        value = jackson.convertToObject(responseBody, Map.class);
        return value.get("access_token");
    }
}
