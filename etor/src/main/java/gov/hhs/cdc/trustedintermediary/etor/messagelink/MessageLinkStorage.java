package gov.hhs.cdc.trustedintermediary.etor.messagelink;

import java.util.Set;

public interface MessageLinkStorage {
    MessageLink getMessageLink(String submissionId) throws MessageLinkException;

    void saveMessageLink(Set<String> messageIds, int linkId) throws MessageLinkException;
}
