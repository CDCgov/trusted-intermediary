package gov.hhs.cdc.trustedintermediary.etor.results;

import gov.hhs.cdc.trustedintermediary.etor.messages.SendMessageUseCase;
import gov.hhs.cdc.trustedintermediary.etor.messages.UnableToSendMessageException;
import gov.hhs.cdc.trustedintermediary.etor.metadata.EtorMetadataStep;
import gov.hhs.cdc.trustedintermediary.wrappers.MetricMetadata;
import javax.inject.Inject;

/** Use case for converting and sending a lab result message. */
public class SendResultUseCase implements SendMessageUseCase<Result<?>> {
    private static final SendResultUseCase INSTANCE = new SendResultUseCase();

    @Inject ResultConverter converter;
    @Inject ResultSender sender;
    @Inject MetricMetadata metadata;

    private SendResultUseCase() {}

    public static SendResultUseCase getInstance() {
        return INSTANCE;
    }

    @Override
    public void convertAndSend(Result<?> result) throws UnableToSendMessageException {

        // savePartnerMetadataForReceivedResult(receivedSubmissionId, result)

        var convertedResult = converter.addEtorProcessingTag(result);
        metadata.put(
                result.getFhirResourceId(),
                EtorMetadataStep.ETOR_PROCESSING_TAG_ADDED_TO_MESSAGE_HEADER);

        String sentSubmissionId = sender.send(convertedResult).orElse(null);

        // saveSentResultSubmissionId(receivedSubmissionId, bundleId)
    }
}
