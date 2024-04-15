package gov.hhs.cdc.trustedintermediary.etor.messagelink;

import java.util.Optional;

/** This interface defines the methods for storing and retrieving message links. */
public interface MessageLinkStorage {
    Optional<MessageLink> getMessageLink(String messageId) throws MessageLinkException;

    void saveMessageLink(MessageLink messageLink) throws MessageLinkException;
}
