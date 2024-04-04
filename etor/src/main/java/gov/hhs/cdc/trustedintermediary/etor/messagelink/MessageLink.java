package gov.hhs.cdc.trustedintermediary.etor.messagelink;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class MessageLink {
    private final Integer linkId;
    private final Set<String> messageIds;

    public MessageLink(Integer linkId) {
        this.linkId = linkId;
        this.messageIds = new HashSet<>();
    }

    public MessageLink(Integer linkId, String messageId) {
        this(linkId);
        this.messageIds.add(messageId);
    }

    public MessageLink(Integer linkId, Set<String> messageIds) {
        this(linkId);
        this.messageIds.addAll(messageIds);
    }

    public Integer getLinkId() {
        return linkId;
    }

    public Set<String> getMessageIds() {
        return Collections.unmodifiableSet(messageIds);
    }

    public void addMessageId(String messageId) {
        this.messageIds.add(messageId);
    }

    public void addMessageIds(Set<String> messageIds) {
        this.messageIds.addAll(messageIds);
    }
}
