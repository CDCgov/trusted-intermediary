package gov.hhs.cdc.trustedintermediary.etor.messagelink;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class MessageLink {
    private Integer linkId;
    private Set<String> messageIds;

    public MessageLink() {
        this.messageIds = new HashSet<>();
    }

    public MessageLink(Integer linkId, String messageId) {
        this.linkId = linkId;
        this.messageIds = new HashSet<>(Collections.singleton(messageId));
    }

    public MessageLink(Integer linkId, Set<String> messageIds) {
        this.linkId = linkId;
        this.messageIds = new HashSet<>(messageIds);
    }

    public void setLinkId(Integer linkId) {
        this.linkId = linkId;
    }

    public Integer getLinkId() {
        return linkId;
    }

    public void setMessageIds(Set<String> messageIds) {
        this.messageIds = new HashSet<>(messageIds);
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
