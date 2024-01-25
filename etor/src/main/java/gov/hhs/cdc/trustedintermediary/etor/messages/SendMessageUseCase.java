package gov.hhs.cdc.trustedintermediary.etor.messages;

/**
 * This interface represents a use case for sending a generic message. It provides a method to
 * convert and send the message.
 *
 * @param <T> the type of message to be sent
 */
public interface SendMessageUseCase<T> {

    void convertAndSend(final T message, String receivedSubmissionId)
            throws UnableToSendMessageException;
}
