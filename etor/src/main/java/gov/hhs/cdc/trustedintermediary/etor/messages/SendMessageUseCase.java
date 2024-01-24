package gov.hhs.cdc.trustedintermediary.etor.messages;

public interface SendMessageUseCase<T> {

    void convertAndSend(final T message, String receivedSubmissionId)
            throws UnableToSendMessageException;
}
