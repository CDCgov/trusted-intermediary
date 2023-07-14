package gov.hhs.cdc.trustedintermediary.external.reportstream;

import gov.hhs.cdc.trustedintermediary.context.ApplicationContext;
import gov.hhs.cdc.trustedintermediary.etor.orders.Order;
import gov.hhs.cdc.trustedintermediary.etor.orders.OrderSender;
import gov.hhs.cdc.trustedintermediary.etor.orders.UnableToSendOrderException;
import gov.hhs.cdc.trustedintermediary.wrappers.AuthEngine;
import gov.hhs.cdc.trustedintermediary.wrappers.Cache;
import gov.hhs.cdc.trustedintermediary.wrappers.HapiFhir;
import gov.hhs.cdc.trustedintermediary.wrappers.HttpClient;
import gov.hhs.cdc.trustedintermediary.wrappers.HttpClientException;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import gov.hhs.cdc.trustedintermediary.wrappers.SecretRetrievalException;
import gov.hhs.cdc.trustedintermediary.wrappers.Secrets;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.Formatter;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.FormatterProcessingException;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.TypeReference;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.inject.Inject;

/** Accepts a {@link Order} and sends it to ReportStream. */
public class ReportStreamOrderSender implements OrderSender {

    private static final ReportStreamOrderSender INSTANCE = new ReportStreamOrderSender();

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

    @Inject private HttpClient client;
    @Inject private AuthEngine jwt;
    @Inject private Formatter formatter;
    @Inject private HapiFhir fhir;
    @Inject private Logger logger;
    @Inject private Secrets secrets;
    @Inject private Cache keyCache;

    public static ReportStreamOrderSender getInstance() {
        return INSTANCE;
    }

    private ReportStreamOrderSender() {}

    @Override
    public void sendOrder(final Order<?> order) throws UnableToSendOrderException {
        logger.logInfo("Sending the order to ReportStream at {}", RS_DOMAIN_NAME);

        String json = fhir.encodeResourceToJson(order.getUnderlyingOrder());
        String bearerToken = getRsToken();
        String rsResponseBody = sendRequestBody(json, bearerToken);
        logRsSubmissionId(rsResponseBody);
    }

    private void logRsSubmissionId(String rsResponseBody) {
        try {
            var rsResponse =
                    formatter.convertJsonToObject(
                            rsResponseBody, new TypeReference<ReportStreamSubmissionResponse>() {});
            logger.logInfo(
                    "Order successfully sent, ReportStream submissionId={}",
                    rsResponse.submissionId());
        } catch (FormatterProcessingException e) {
            logger.logError("Unable to log RS response", e);
        }
    }

    protected String getRsToken() throws UnableToSendOrderException {
        logger.logInfo("Looking up ReportStream token");
        if (getRsTokenCache() != null && isValidToken()) {
            logger.logDebug("valid cache token");
            return getRsTokenCache();
        }

        String token = requestToken();
        setRsTokenCache(token);

        return token;
    }

    protected boolean isValidToken() {
        String token = getRsTokenCache();
        LocalDateTime expirationDate = jwt.getExpirationDate(token);

        return LocalDateTime.now().isBefore(expirationDate.minus(15, ChronoUnit.SECONDS));
    }

    protected String sendRequestBody(@Nonnull String json, @Nonnull String bearerToken)
            throws UnableToSendOrderException {
        logger.logInfo("Sending payload to ReportStream");

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
            throw new UnableToSendOrderException("Error POSTing the payload to ReportStream", e);
        }

        return res;
    }

    protected String requestToken() throws UnableToSendOrderException {
        logger.logInfo("Requesting token from ReportStream");

        String senderToken = null;
        String token = "";
        String body;
        Map<String, String> headers = Map.of("Content-Type", "application/x-www-form-urlencoded");
        try {
            senderToken =
                    jwt.generateToken(
                            CLIENT_NAME,
                            CLIENT_NAME,
                            CLIENT_NAME,
                            RS_DOMAIN_NAME,
                            300,
                            retrievePrivateKey());
            body = composeRequestBody(senderToken);
            String rsResponse = client.post(RS_AUTH_API_URL, headers, body);
            token = extractToken(rsResponse);
        } catch (Exception e) {
            throw new UnableToSendOrderException(
                    "Error getting the API token from ReportStream", e);
        }
        return token;
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

    protected String extractToken(String responseBody) throws FormatterProcessingException {
        var value =
                formatter.convertJsonToObject(
                        responseBody, new TypeReference<Map<String, String>>() {});
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
