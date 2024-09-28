package gov.hhs.cdc.trustedintermediary.etor.messages;

/**
 * Represents an identifier triplet, consisting of a code, display value, and coding system.
 * Identifiers are used in HL7 fields such as OBR-4 and OBX-3. These fields contain two identifier
 * triplets, one in subfields 1/2/3, and an alternate in subfields 4/5/6. For reference, see: <a
 * href="https://hl7-definition.caristix.com/v2/HL7v2.5.1/Fields/OBX.3"</a>
 */
public record IdentifierCode(String code, String display, String codingSystem) {}
