package gov.hhs.cdc.trustedintermediary.etor.messages;

import java.util.Optional;

public interface MessageSender<T> {
    Optional<String> send(T message) throws UnableToSendMessageException;
}
