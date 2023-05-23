package gov.hhs.cdc.trustedintermediary.external.reportstream;

import gov.hhs.cdc.trustedintermediary.context.ApplicationContext;
import gov.hhs.cdc.trustedintermediary.etor.KeyCache;
import gov.hhs.cdc.trustedintermediary.etor.demographics.LabOrder;
import gov.hhs.cdc.trustedintermediary.etor.demographics.LabOrderSender;
import gov.hhs.cdc.trustedintermediary.etor.demographics.UnableToSendLabOrderException;
import gov.hhs.cdc.trustedintermediary.wrappers.AuthEngine;
import gov.hhs.cdc.trustedintermediary.wrappers.Formatter;
import gov.hhs.cdc.trustedintermediary.wrappers.FormatterProcessingException;
import gov.hhs.cdc.trustedintermediary.wrappers.HapiFhir;
import gov.hhs.cdc.trustedintermediary.wrappers.HttpClient;
import gov.hhs.cdc.trustedintermediary.wrappers.HttpClientException;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import gov.hhs.cdc.trustedintermediary.wrappers.SecretRetrievalException;
import gov.hhs.cdc.trustedintermediary.wrappers.Secrets;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.inject.Inject;

/** Accepts a {@link LabOrder} and sends it to ReportStream. */
public class ReportStreamLabOrderSender implements LabOrderSender {

    private static final ReportStreamLabOrderSender INSTANCE = new ReportStreamLabOrderSender();

    private static final String RS_URL_PREFIX_PROPERTY = "REPORT_STREAM_URL_PREFIX";
    private static final String RS_WATERS_API_URL =
            ApplicationContext.getProperty(RS_URL_PREFIX_PROPERTY) + "/api/waters";
    private static final String RS_AUTH_API_URL =
            ApplicationContext.getProperty(RS_URL_PREFIX_PROPERTY) + "/api/token";
    private static final String RS_DOMAIN_NAME =
            Optional.ofNullable(ApplicationContext.getProperty(RS_URL_PREFIX_PROPERTY))
                    .map(urlPrefix -> urlPrefix.replace("https://", "").replace("http://", ""))
                    .orElse("");

    private static final String CLIENT_NAME = "flexion.etor-service-sender";

    private String rsTokenCache;

    protected synchronized String getRsTokenCache() {
        return this.rsTokenCache;
    }

    protected synchronized void setRsTokenCache(String token) {
        this.rsTokenCache = token;
    }

    private String cachedPrivateKey;

    protected synchronized String getCachedPrivateKey() {
        return cachedPrivateKey;
    }

    protected synchronized void setCachedPrivateKey(String cachedPrivateKey) {
        this.cachedPrivateKey = cachedPrivateKey;
    }

    @Inject private HttpClient client;
    @Inject private AuthEngine jwt;
    @Inject private Formatter jackson;
    @Inject private HapiFhir fhir;
    @Inject private Logger logger;
    @Inject private Secrets secrets;
    @Inject private KeyCache cache;

    public static ReportStreamLabOrderSender getInstance() {
        return INSTANCE;
    }

    private ReportStreamLabOrderSender() {}

    @Override
    public void sendOrder(final LabOrder<?> order) throws UnableToSendLabOrderException {
        logger.logInfo("Sending the order to ReportStream at {}", RS_DOMAIN_NAME);

        String json = fhir.encodeResourceToJson(order.getUnderlyingOrder());
        String bearerToken = getRsToken();
        sendRequestBody(json, bearerToken);
    }

    protected String getRsToken() throws UnableToSendLabOrderException {
        logger.logInfo("getting Report Stream token...");
        if (getRsTokenCache() != null && isValidToken()) {
            logger.logDebug("valid cache token");
            return getRsTokenCache();
        }

        logger.logDebug("requesting a new token...");
        String token = requestToken();
        logger.logDebug("token request successful");
        setRsTokenCache(token);

        return token;
    }

    protected boolean isValidToken() {
        String token = getRsTokenCache();
        LocalDateTime expirationDate = jwt.getExpirationDate(token);

        return LocalDateTime.now().isBefore(expirationDate.minus(15, ChronoUnit.SECONDS));
    }

    protected String sendRequestBody(@Nonnull String json, @Nonnull String bearerToken)
            throws UnableToSendLabOrderException {
        logger.logInfo("Sending to payload to ReportStream");

        String res = "";
        Map<String, String> headers =
                Map.of(
                        "Authorization",
                        "Bearer " + bearerToken,
                        "client",
                        CLIENT_NAME,
                        "Content-Type",
                        "application/fhir+ndjson");
        try {
            res = client.post(RS_WATERS_API_URL, headers, json);
        } catch (HttpClientException e) {
            throw new UnableToSendLabOrderException("Error POSTing the payload to ReportStream", e);
        }

        return res;
    }

    protected String requestToken() throws UnableToSendLabOrderException {
        logger.logInfo("Requesting token from ReportStream");

        String senderToken = null;
        String token = "";
        String body;
        Map<String, String> headers = Map.of("Content-Type", "application/x-www-form-urlencoded");
        try {
            senderToken =
                    jwt.generateSenderToken(
                            CLIENT_NAME, RS_DOMAIN_NAME, retrievePrivateKey(), CLIENT_NAME, 300);
            body = composeRequestBody(senderToken);
            String rsResponse = client.post(RS_AUTH_API_URL, headers, body);
            token = extractToken(rsResponse);
        } catch (Exception e) {
            throw new UnableToSendLabOrderException(
                    "Error getting the API token from ReportStream", e);
        }
        return token;
    }

    protected String retrievePrivateKey() throws SecretRetrievalException {
        var senderPrivateKey =
                "report-stream-sender-private-key-" + ApplicationContext.getEnvironment();
        String key = this.cache.get(senderPrivateKey);
        if (key != null) {
            return key;
        }

        key = secrets.getKey(senderPrivateKey);
        this.cache.put(senderPrivateKey, key);
        return key;
    }

    protected String extractToken(String responseBody) throws FormatterProcessingException {

        Map<String, String> value;

        value = jackson.convertToObject(responseBody, Map.class);
        return value.get("access_token");
    }

    protected String composeRequestBody(String senderToken) {
        String scope = "flexion.*.report";
        String grantType = "client_credentials";
        String clientAssertionType = "urn:ietf:params:oauth:client-assertion-type:jwt-bearer";
        return "scope="
                + scope
                + "&grant_type="
                + grantType
                + "&client_assertion_type="
                + clientAssertionType
                + "&client_assertion="
                + senderToken;
    }
}
