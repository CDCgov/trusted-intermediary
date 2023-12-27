package gov.hhs.cdc.trustedintermediary.external.reportstream;

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

    @Inject private ReportStreamEndpointClient rsclient;
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
        //        logger.logInfo("Sending the order to ReportStream at {}", RS_DOMAIN_NAME);
        String json = fhir.encodeResourceToJson(order.getUnderlyingOrder());
        String bearerToken;
        String rsResponseBody;
        try {
            bearerToken = rsclient.getRsToken();
            rsResponseBody = rsclient.requestWatersEndpoint(json, bearerToken);
        } catch (ReportStreamEndpointClientException e) {
            throw new UnableToSendOrderException("Unable to send order to ReportStream", e);
        }
        Optional<String> submissionId = getSubmissionId(rsResponseBody);
        logger.logInfo("Order successfully sent, ReportStream submissionId={}", submissionId);
        metadata.put(order.getFhirResourceId(), EtorMetadataStep.SENT_TO_REPORT_STREAM);
        return submissionId;
    }

    protected Optional<String> getSubmissionId(String rsResponseBody) {
        try {
            var rsResponse =
                    formatter.convertJsonToObject(
                            rsResponseBody, new TypeReference<Map<String, Object>>() {});
            return Optional.ofNullable((String) rsResponse.get("submissionId"));
        } catch (FormatterProcessingException e) {
            logger.logError("Unable to get the submissionId", e);
        }

        return Optional.empty();
    }
}
