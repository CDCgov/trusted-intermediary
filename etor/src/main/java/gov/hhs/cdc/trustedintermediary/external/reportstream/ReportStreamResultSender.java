package gov.hhs.cdc.trustedintermediary.external.reportstream;

import gov.hhs.cdc.trustedintermediary.etor.messages.UnableToSendMessageException;
import gov.hhs.cdc.trustedintermediary.etor.results.Result;
import gov.hhs.cdc.trustedintermediary.etor.results.ResultSender;
import gov.hhs.cdc.trustedintermediary.wrappers.HapiFhir;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import java.util.Optional;
import javax.inject.Inject;

/**
 * This class is responsible for sending results to the ReportStream service and receiving a
 * response back.
 */
public class ReportStreamResultSender implements ResultSender {

    private static final ReportStreamResultSender INSTANCE = new ReportStreamResultSender();

    @Inject private ReportStreamSenderHelper sender;
    @Inject private HapiFhir fhir;
    @Inject private Logger logger;

    public static ReportStreamResultSender getInstance() {
        return INSTANCE;
    }

    private ReportStreamResultSender() {}

    @Override
    public Optional<String> send(Result<?> result) throws UnableToSendMessageException {
        logger.logInfo("Sending results to ReportStream");
        String json = fhir.encodeResourceToJson(result.getUnderlyingResult());
        return sender.sendToReportStream(json, result.getFhirResourceId(), "result");
    }
}
