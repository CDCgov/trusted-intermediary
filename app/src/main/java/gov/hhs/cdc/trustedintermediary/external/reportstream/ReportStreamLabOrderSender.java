package gov.hhs.cdc.trustedintermediary.external.reportstream;

import gov.hhs.cdc.trustedintermediary.etor.demographics.LabOrder;
import gov.hhs.cdc.trustedintermediary.etor.demographics.LabOrderSender;
import gov.hhs.cdc.trustedintermediary.wrappers.*;
import java.io.IOException;
import java.util.Map;
import javax.inject.Inject;
import org.jetbrains.annotations.NotNull;

/** Accepts a {@link LabOrder} and sends it to ReportStream. */
public class ReportStreamLabOrderSender implements LabOrderSender {

    private static final ReportStreamLabOrderSender INSTANCE = new ReportStreamLabOrderSender();
    private String trustedIntermediaryPrivatePemKey = "ENVIRONMENT_SECRET";
    private final String STAGING = "https://staging.prime.cdc.gov/api/waters";
    private final String STAGING_AUTH = "https://staging.prime.cdc.gov/api/token";
    @Inject private HttpClient client;
    @Inject private AuthEngine jwt;
    @Inject private Formatter jackson;
    @Inject private HapiFhir fhir;

    public static ReportStreamLabOrderSender getInstance() {
        return INSTANCE;
    }

    private ReportStreamLabOrderSender() {}

    @Override
    public void sendOrder(final LabOrder<?> order) {
        String json = fhir.encodeResourceToJson(order.getUnderlyingOrder());
        String bearerToken = requestToken();
        sendRequestBody(json, bearerToken);
    }

    protected String sendRequestBody(
            @NotNull String json, @NotNull String bearerToken) { // url param?
        String res = "";
        Map<String, String> headers =
                Map.of(
                        "Authorization", "Bearer " + bearerToken,
                        "client", "flexion",
                        "Content-Type", "application/hl7-v2");
        try {
            res = client.post(this.STAGING, headers, json);
        } catch (IOException e) {
            // TODO exception handling
        }

        return res;
    }

    protected String requestToken() {
        String senderToken = null;
        String token = "";
        String body;
        String sender = "flexion.etor-service-sender";
        String keyId = "flexion.etor-service-sender";
        Map<String, String> headers = Map.of("Content-Type", "application/x-www-form-urlencoded");
        try {
            senderToken = jwt.generateSenderToken(sender, this.STAGING_AUTH, "pemKey", keyId, 300);
            body = composeRequestBody(senderToken);
            String rsResponse = client.post(this.STAGING_AUTH, headers, body);
            // TODO response handling for good structure of response, else it will fail to extract
            // the key
            token = extractToken(rsResponse);
        } catch (Exception e) {
            // TODO exception handling
        }
        return token;
    }

    protected String extractToken(String responseBody) {
        Map<String, String> value = null;
        try {
            value = jackson.convertToObject(responseBody, Map.class);
        } catch (FormatterProcessingException e) {
            // TODO exception handling
        }
        return value.get("access_token");
    }

    protected String composeRequestBody(String senderToken) {
        String scope = "flexion.*.report";
        String grant_type = "client_credentials";
        String client_assertion_type = "urn:ietf:params:oauth:client-assertion-type:jwt-bearer";
        return "scope="
                + scope
                + "&grant_type="
                + grant_type
                + "&client_assertion_type="
                + client_assertion_type
                + "&client_assertion="
                + senderToken;
    }
}
