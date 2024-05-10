package gov.hhs.cdc.trustedintermediary.plugin.path;

/**
 * Enumerates FHIR path expressions for various data elements within a FHIR message. These paths can
 * be used to extract specific pieces of data from a FHIR message, such as identifiers, namespaces,
 * and codes related to sending and receiving facilities and applications.
 */
public enum MappingPath {
    PLACER_ORDER_NUMBER(
            "ORC-2", """
     Bundle.entry.resource.ofType(ServiceRequest).identifier.where(type.coding.code = 'PLAC').value
     """),
    SENDING_FACILITY_NAMESPACE(
            "?-?", """
   Bundle.entry.resource.ofType(MessageHeader).sender.resolve().identifier.where(
   extension.url = 'https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2Field' and
   extension.value = 'HD.1'
   ).value
   """),
    SENDING_FACILITY_UNIVERSAL_ID(
            "?-?","""
   Bundle.entry.resource.ofType(MessageHeader).sender.resolve().identifier.where(
   extension.url = 'https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2Field' and
   extension.value = 'HD.2,HD.3'
   ).value
   """),
    SENDING_FACILITY_UNIVERSAL_ID_TYPE(
            "?-?","""
   Bundle.entry.resource.ofType(MessageHeader).sender.resolve().identifier.where(
   extension.url = 'https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2Field' and
   extension.value = 'HD.2,HD.3'
   ).type.coding.code
   """),
    SENDING_APPLICATION_NAMESPACE(
            "?-?","""
 Bundle.entry.resource.ofType(MessageHeader).source.extension.where(url = 'https://reportstream.cdc.gov/fhir/StructureDefinition/namespace-id').value
 """),
    SENDING_APPLICATION_UNIVERSAL_ID(
            "?-?","""
 Bundle.entry.resource.ofType(MessageHeader).source.extension.where(url = 'https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id').value
 """),
    SENDING_APPLICATION_UNIVERSAL_ID_TYPE(
            "?-?","""
 Bundle.entry.resource.ofType(MessageHeader).source.extension.where(url = 'https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id-type').value
 """),
    RECEIVING_FACILITY_NAMESPACE(
            "?-?","""
   Bundle.entry.resource.ofType(MessageHeader).destination.receiver.resolve().identifier.where(
   extension.url = 'https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2Field' and
   extension.value = 'HD.1'
   ).value
   """),
    RECEIVING_FACILITY_UNIVERSAL_ID(
            "?-?","""
   Bundle.entry.resource.ofType(MessageHeader).destination.receiver.resolve().identifier.where(
   extension.url = 'https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2Field' and
   extension.value = 'HD.2,HD.3'
   ).value
   """),
    RECEIVING_FACILITY_UNIVERSAL_ID_TYPE(
            "?-?","""
   Bundle.entry.resource.ofType(MessageHeader).destination.receiver.resolve().identifier.where(
   extension.url = 'https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2Field' and
   extension.value = 'HD.2,HD.3'
   ).type.coding.code
   """),
    RECEIVING_APPLICATION_NAMESPACE(
            "?-?","""
 Bundle.entry.resource.ofType(MessageHeader).destination.name
 """),
    RECEIVING_APPLICATION_UNIVERSAL_ID(
            "?-?","""
 Bundle.entry.resource.ofType(MessageHeader).destination.extension.where(url = 'https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id').value
 """),
    RECEIVING_APPLICATION_UNIVERSAL_ID_TYPE(
            "?-?","""
 Bundle.entry.resource.ofType(MessageHeader).destination.extension.where(url = 'https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id-type').value
 """);

    private final String fhirPath;
    private final String hl7v2Path;

    MappingPath(String hl7v2Path, String fhirPath) {
        this.hl7v2Path = hl7v2Path;
        this.fhirPath = fhirPath;
    }

    public String getHl7v2Path() {
        return hl7v2Path;
    }

    public String getFhirPath() {
        return fhirPath;
    }
}

