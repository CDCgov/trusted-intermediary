package gov.hhs.cdc.trustedintermediary.etor.messages;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MessageDetails {
    private String namespace;
    private String universalId;
    private String universalIdType;

    public MessageDetails(String namespace, String universalId, String universalIdType) {
        this.namespace = namespace;
        this.universalId = universalId;
        this.universalIdType = universalIdType;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getUniversalId() {
        return universalId;
    }

    public void setUniversalId(String universalId) {
        this.universalId = universalId;
    }

    public String getUniversalIdType() {
        return universalIdType;
    }

    public void setUniversalIdType(String universalIdType) {
        this.universalIdType = universalIdType;
    }

    @Override
    public String toString() {
        return Stream.of(this.namespace, this.universalId, this.universalIdType)
                .filter(Objects::nonNull)
                .collect(Collectors.joining("^"));
    }
}