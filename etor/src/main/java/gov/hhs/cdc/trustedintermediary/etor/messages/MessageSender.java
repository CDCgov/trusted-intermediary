package gov.hhs.cdc.trustedintermediary.etor.messages;

import java.util.Optional;

/** Interface for sending a generic message. */
public interface MessageSender<T> {
    Optional<String> send(T message) throws UnableToSendMessageException;
}
