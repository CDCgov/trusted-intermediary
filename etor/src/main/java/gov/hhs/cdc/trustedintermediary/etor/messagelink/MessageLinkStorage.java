package gov.hhs.cdc.trustedintermediary.etor.messagelink;

import java.util.Optional;

public interface MessageLinkStorage {
    Optional<MessageLink> getMessageLink(String submissionId) throws MessageLinkException;

    void saveMessageLink(MessageLink messageLink) throws MessageLinkException;
}
