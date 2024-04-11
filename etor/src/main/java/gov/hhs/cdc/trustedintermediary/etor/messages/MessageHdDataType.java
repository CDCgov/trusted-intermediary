package gov.hhs.cdc.trustedintermediary.etor.messages;

/**
 * This record class represents the result of evaluating a FHIRPath expression, encapsulating
 * specific details extracted from a FHIR resource. This class holds values for namespace, universal
 * identifier (ID), and the type of the universal ID. HD reference: <a
 * href="https://hl7-definition.caristix.com/v2/HL7v2.5.1/DataTypes/HD">HD-DataType</a>
 */
public record MessageHdDataType(String namespace, String universalId, String universalIdType) {

    // Constructor for Jackson Mapping
    public MessageHdDataType {}
}
