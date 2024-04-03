package gov.hhs.cdc.trustedintermediary.etor.messages;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class represents the result of evaluating a FHIRPath expression, encapsulating specific
 * details extracted from a FHIR resource. This class holds values for namespace, universal
 * identifier (ID), and the type of the universal ID, providing a mechanism to output these details
 * in a concatenated string format. HD reference: <a
 * href="https://hl7-definition.caristix.com/v2/HL7v2.5.1/DataTypes/HD">HD-DataType</a>
 */
public class MessageHdDataType {
    private String namespace;
    private String universalId;
    private String universalIdType;

    public MessageHdDataType(String namespace, String universalId, String universalIdType) {
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
        List<String> filteredValues =
                Stream.of(this.namespace, this.universalId, this.universalIdType)
                        .filter(s -> s != null && !s.isEmpty())
                        .collect(Collectors.toList());
        return filteredValues.isEmpty() ? "" : String.join("^", filteredValues);
    }
}
