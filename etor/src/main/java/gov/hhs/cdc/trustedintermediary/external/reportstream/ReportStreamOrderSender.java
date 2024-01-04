package gov.hhs.cdc.trustedintermediary.external.reportstream;

import gov.hhs.cdc.trustedintermediary.etor.RSEndpointClient;
import gov.hhs.cdc.trustedintermediary.etor.metadata.EtorMetadataStep;
import gov.hhs.cdc.trustedintermediary.etor.orders.Order;
import gov.hhs.cdc.trustedintermediary.etor.orders.OrderSender;
import gov.hhs.cdc.trustedintermediary.etor.orders.UnableToSendOrderException;
import gov.hhs.cdc.trustedintermediary.wrappers.HapiFhir;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import gov.hhs.cdc.trustedintermediary.wrappers.MetricMetadata;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.Formatter;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.FormatterProcessingException;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.TypeReference;
import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;

/** Accepts a {@link Order} and sends it to ReportStream. */
public class ReportStreamOrderSender implements OrderSender {

    private static final ReportStreamOrderSender INSTANCE = new ReportStreamOrderSender();

    @Inject private RSEndpointClient rsclient;
    @Inject private Formatter formatter;
    @Inject private HapiFhir fhir;
    @Inject private Logger logger;
    @Inject MetricMetadata metadata;

    public static ReportStreamOrderSender getInstance() {
        return INSTANCE;
    }

    private ReportStreamOrderSender() {}

    @Override
    public Optional<String> sendOrder(final Order<?> order) throws UnableToSendOrderException {
        logger.logInfo("Sending the order to ReportStream");

        String json = fhir.encodeResourceToJson(order.getUnderlyingOrder());
        String bearerToken;
        String rsResponseBody;

        try {
            bearerToken = rsclient.getRsToken();
            rsResponseBody = rsclient.requestWatersEndpoint(json, bearerToken);
        } catch (ReportStreamEndpointClientException e) {
            throw new UnableToSendOrderException("Unable to send order to ReportStream", e);
        }

        logger.logInfo("Order successfully sent to ReportStream");
        metadata.put(order.getFhirResourceId(), EtorMetadataStep.SENT_TO_REPORT_STREAM);

        Optional<String> sentSubmissionId = getSubmissionId(rsResponseBody);
        if (sentSubmissionId.isEmpty()) {
            logger.logError("Unable to retrieve sentSubmissionId from ReportStream response");
        } else {
            logger.logInfo("ReportStream response's sentSubmissionId={}", sentSubmissionId);
        }

        return sentSubmissionId;
    }

    protected Optional<String> getSubmissionId(String rsResponseBody) {
        try {
            Map<String, Object> rsResponse =
                    formatter.convertJsonToObject(rsResponseBody, new TypeReference<>() {});
            return Optional.ofNullable(rsResponse.get("submissionId").toString());
        } catch (FormatterProcessingException e) {
            logger.logError("Unable to get the submissionId", e);
        }

        return Optional.empty();
    }
}
