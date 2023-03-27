package gov.hhs.cdc.trustedintermediary.external.reportstream;

import gov.hhs.cdc.trustedintermediary.context.ApplicationContext;
import gov.hhs.cdc.trustedintermediary.etor.demographics.LabOrder;
import gov.hhs.cdc.trustedintermediary.etor.demographics.LabOrderSender;
import gov.hhs.cdc.trustedintermediary.wrappers.AuthEngine;
import gov.hhs.cdc.trustedintermediary.wrappers.Formatter;
import gov.hhs.cdc.trustedintermediary.wrappers.FormatterProcessingException;
import gov.hhs.cdc.trustedintermediary.wrappers.HapiFhir;
import gov.hhs.cdc.trustedintermediary.wrappers.HttpClient;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import java.io.IOException;
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

    @Inject private HttpClient client;
    @Inject private AuthEngine jwt;
    @Inject private Formatter jackson;
    @Inject private HapiFhir fhir;
    @Inject private Logger logger;

    public static ReportStreamLabOrderSender getInstance() {
        return INSTANCE;
    }

    private ReportStreamLabOrderSender() {}

    @Override
    public void sendOrder(final LabOrder<?> order) {
        logger.logInfo("Sending the order to ReportStream at {}", RS_DOMAIN_NAME);

        String json = fhir.encodeResourceToJson(order.getUnderlyingOrder());
        String bearerToken = requestToken();
        sendRequestBody(json, bearerToken);
    }

    protected String sendRequestBody(@Nonnull String json, @Nonnull String bearerToken) {
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
        } catch (IOException e) {
            logger.logError("Error POSTing the payload to ReportStream", e);
            // TODO exception handling
        }

        return res;
    }

    protected String requestToken() {
        logger.logInfo("Requesting token from ReportStream");

        String senderToken = null;
        String token = "";
        String body;
        Map<String, String> headers = Map.of("Content-Type", "application/x-www-form-urlencoded");
        try {
            senderToken =
                    jwt.generateSenderToken(
                            CLIENT_NAME, RS_DOMAIN_NAME, "pemKey", CLIENT_NAME, 300);
            body = composeRequestBody(senderToken);
            String rsResponse = client.post(RS_AUTH_API_URL, headers, body);
            // TODO response handling for good structure of response, else it will fail to extract
            // the key
            token = extractToken(rsResponse);
        } catch (Exception e) {
            logger.logError("Error getting the API token from ReportStream", e);
            // TODO exception handling
        }
        return token;
    }

    protected String extractToken(String responseBody) {
        Map<String, String> value;
        try {
            value = jackson.convertToObject(responseBody, Map.class);
            return value.get("access_token");
        } catch (FormatterProcessingException | ClassCastException e) {
            logger.logError("Error parsing the ReportStream auth token response body", e);
            // TODO exception handling
        }
        return "";
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
