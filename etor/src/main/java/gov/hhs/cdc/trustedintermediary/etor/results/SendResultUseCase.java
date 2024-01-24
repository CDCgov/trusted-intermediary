package gov.hhs.cdc.trustedintermediary.etor.results;

import gov.hhs.cdc.trustedintermediary.etor.messages.MessageSender;
import gov.hhs.cdc.trustedintermediary.etor.messages.SendMessageUseCase;
import gov.hhs.cdc.trustedintermediary.etor.messages.UnableToSendMessageException;
import javax.inject.Inject;

public class SendResultUseCase implements SendMessageUseCase<Result<?>> {
    private static final SendResultUseCase INSTANCE = new SendResultUseCase();
    @Inject MessageSender<Result<?>> sender;

    private SendResultUseCase() {}

    public static SendResultUseCase getInstance() {
        return INSTANCE;
    }

    @Override
    public void convertAndSend(Result<?> result, String receivedSubmissionId)
            throws UnableToSendMessageException {
        sender.send(result);
    }
}
