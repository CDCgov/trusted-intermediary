package gov.hhs.cdc.trustedintermediary.plugin.path;

/**
 * Enumerates FHIR and HL7 path expressions for various data elements within a FHIR message. These
 * paths can be used to extract specific pieces of data from a FHIR message, such as identifiers,
 * namespaces, and codes related to sending and receiving facilities and applications. It also
 * defines the HL7 field names.
 */
public enum Hl7FhirMappingPath {
    PLACER_ORDER_NUMBER(
            "ORC.2",
            """
     Bundle.entry.resource.ofType(ServiceRequest).identifier.where(type.coding.code = 'PLAC').value
     """),
    SENDING_FACILITY_NAMESPACE(
            "",
            """
   Bundle.entry.resource.ofType(MessageHeader).sender.resolve().identifier.where(
   extension.url = 'https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2Field' and
   extension.value = 'HD.1'
   ).value
   """),
    SENDING_FACILITY_UNIVERSAL_ID(
            "",
            """
   Bundle.entry.resource.ofType(MessageHeader).sender.resolve().identifier.where(
   extension.url = 'https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2Field' and
   extension.value = 'HD.2,HD.3'
   ).value
   """),
    SENDING_FACILITY_UNIVERSAL_ID_TYPE(
            "",
            """
   Bundle.entry.resource.ofType(MessageHeader).sender.resolve().identifier.where(
   extension.url = 'https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2Field' and
   extension.value = 'HD.2,HD.3'
   ).type.coding.code
   """),
    SENDING_APPLICATION_NAMESPACE(
            "",
            """
 Bundle.entry.resource.ofType(MessageHeader).source.extension.where(url = 'https://reportstream.cdc.gov/fhir/StructureDefinition/namespace-id').value
 """),
    SENDING_APPLICATION_UNIVERSAL_ID(
            "",
            """
 Bundle.entry.resource.ofType(MessageHeader).source.extension.where(url = 'https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id').value
 """),
    SENDING_APPLICATION_UNIVERSAL_ID_TYPE(
            "",
            """
 Bundle.entry.resource.ofType(MessageHeader).source.extension.where(url = 'https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id-type').value
 """),
    RECEIVING_FACILITY_NAMESPACE(
            "",
            """
   Bundle.entry.resource.ofType(MessageHeader).destination.receiver.resolve().identifier.where(
   extension.url = 'https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2Field' and
   extension.value = 'HD.1'
   ).value
   """),
    RECEIVING_FACILITY_UNIVERSAL_ID(
            "",
            """
   Bundle.entry.resource.ofType(MessageHeader).destination.receiver.resolve().identifier.where(
   extension.url = 'https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2Field' and
   extension.value = 'HD.2,HD.3'
   ).value
   """),
    RECEIVING_FACILITY_UNIVERSAL_ID_TYPE(
            "",
            """
   Bundle.entry.resource.ofType(MessageHeader).destination.receiver.resolve().identifier.where(
   extension.url = 'https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2Field' and
   extension.value = 'HD.2,HD.3'
   ).type.coding.code
   """),
    RECEIVING_APPLICATION_NAMESPACE(
            "", """
 Bundle.entry.resource.ofType(MessageHeader).destination.name
 """),
    RECEIVING_APPLICATION_UNIVERSAL_ID(
            "",
            """
 Bundle.entry.resource.ofType(MessageHeader).destination.extension.where(url = 'https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id').value
 """),
    RECEIVING_APPLICATION_UNIVERSAL_ID_TYPE(
            "",
            """
 Bundle.entry.resource.ofType(MessageHeader).destination.extension.where(url = 'https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id-type').value
 """),
    PATIENT_IDENTIFIER_NAME_TYPE_CODE(
            "PID.5-7",
            " Bundle.entry.resource.ofType(Patient).name.extension.where(url = 'https://reportstream.cdc.gov/fhir/StructureDefinition/xpn-human-name').extension.where(url = 'XPN.7').value");

    private final String fhirPath;
    private final String hl7Path;

    Hl7FhirMappingPath(String hl7Path, String fhirPath) {
        this.hl7Path = hl7Path;
        this.fhirPath = fhirPath;
    }

    public String getHl7Path() {
        return hl7Path;
    }

    public String getFhirPath() {
        return fhirPath;
    }
}
