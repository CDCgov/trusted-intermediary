package gov.hhs.cdc.trustedintermediary.etor.messagelink;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * This class represents a link between messages. Each link has a unique ID and is associated with a
 * set of message IDs to link.
 */
public final class MessageLink {
    private UUID linkId;
    private Set<String> messageIds;

    public MessageLink() {
        this.messageIds = new HashSet<>();
    }

    public MessageLink(UUID linkId, String messageId) {
        this.linkId = linkId;
        this.messageIds = Set.of(messageId);
    }

    public MessageLink(UUID linkId, Set<String> messageIds) {
        this.linkId = linkId;
        this.messageIds = new HashSet<>(messageIds);
    }

    public void setLinkId(UUID linkId) {
        this.linkId = linkId;
    }

    public UUID getLinkId() {
        return linkId;
    }

    public void setMessageIds(Set<String> messageIds) {
        this.messageIds = messageIds;
    }

    public Set<String> getMessageIds() {
        return this.messageIds;
    }

    public void addMessageId(String messageId) {
        this.messageIds.add(messageId);
    }

    public void addMessageIds(Set<String> messageIds) {
        this.messageIds.addAll(messageIds);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof final MessageLink that)) {
            return false;
        }
        return Objects.equals(getLinkId(), that.getLinkId())
                && Objects.equals(getMessageIds(), that.getMessageIds());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getLinkId(), getMessageIds());
    }
}
