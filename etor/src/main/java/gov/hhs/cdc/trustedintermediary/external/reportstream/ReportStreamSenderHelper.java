package gov.hhs.cdc.trustedintermediary.external.reportstream;

import gov.hhs.cdc.trustedintermediary.etor.RSEndpointClient;
import gov.hhs.cdc.trustedintermediary.etor.messages.UnableToSendMessageException;
import gov.hhs.cdc.trustedintermediary.etor.metadata.EtorMetadataStep;
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataMessageType;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import gov.hhs.cdc.trustedintermediary.wrappers.MetricMetadata;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.Formatter;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.FormatterProcessingException;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.TypeReference;
import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;

/** Helper class for sending messages to ReportStream */
public class ReportStreamSenderHelper {
    private static final ReportStreamSenderHelper INSTANCE = new ReportStreamSenderHelper();

    @Inject RSEndpointClient rsclient;
    @Inject Formatter formatter;
    @Inject Logger logger;
    @Inject MetricMetadata metadata;

    private ReportStreamSenderHelper() {}

    public static ReportStreamSenderHelper getInstance() {
        return INSTANCE;
    }

    public Optional<String> sendOrderToReportStream(String body, String fhirResourceId)
            throws UnableToSendMessageException {
        return sendToReportStream(body, fhirResourceId, PartnerMetadataMessageType.ORDER);
    }

    public Optional<String> sendResultToReportStream(String body, String fhirResourceId)
            throws UnableToSendMessageException {
        return sendToReportStream(body, fhirResourceId, PartnerMetadataMessageType.RESULT);
    }

    protected Optional<String> sendToReportStream(
            String body, String fhirResourceId, PartnerMetadataMessageType messageType)
            throws UnableToSendMessageException {
        String bearerToken;
        String rsResponseBody;

        try {
            bearerToken = rsclient.getRsToken();
            rsResponseBody = rsclient.requestWatersEndpoint(body, bearerToken);
        } catch (ReportStreamEndpointClientException e) {
            throw new UnableToSendMessageException(
                    "Unable to send " + messageType + " to ReportStream", e);
        }

        logger.logInfo("{} successfully sent to ReportStream", messageType);
        metadata.put(fhirResourceId, EtorMetadataStep.SENT_TO_REPORT_STREAM);

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
