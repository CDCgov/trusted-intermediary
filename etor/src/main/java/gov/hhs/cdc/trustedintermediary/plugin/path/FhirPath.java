package gov.hhs.cdc.trustedintermediary.plugin.path;

public enum FhirPath {
    PLACER_ORDER_NUMBER(
            """
     Bundle.entry.resource.ofType(ServiceRequest).identifier.where(type.coding.code = 'PLAC').value
     """),
    SENDING_FACILITY_NAMESPACE(
            """
   Bundle.entry.resource.ofType(MessageHeader).sender.resolve().identifier.where(
   extension.url = 'https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2Field' and
   extension.value = 'HD.1'
   ).value
   """),
    SENDING_FACILITY_UNIVERSAL_ID(
            """
   Bundle.entry.resource.ofType(MessageHeader).sender.resolve().identifier.where(
   extension.url = 'https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2Field' and
   extension.value = 'HD.2,HD.3'
   ).value
   """),
    SENDING_FACILITY_UNIVERSAL_ID_TYPE(
            """
   Bundle.entry.resource.ofType(MessageHeader).sender.resolve().identifier.where(
   extension.url = 'https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2Field' and
   extension.value = 'HD.2,HD.3'
   ).type.coding.code
   """),
    SENDING_APPLICATION_NAMESPACE(
            """
 Bundle.entry.resource.ofType(MessageHeader).source.extension.where(url = 'https://reportstream.cdc.gov/fhir/StructureDefinition/namespace-id').value
 """),
    SENDING_APPLICATION_UNIVERSAL_ID(
            """
 Bundle.entry.resource.ofType(MessageHeader).source.extension.where(url = 'https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id').value
 """),
    SENDING_APPLICATION_UNIVERSAL_ID_TYPE(
            """
 Bundle.entry.resource.ofType(MessageHeader).source.extension.where(url = 'https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id-type').value
 """),
    RECEIVING_FACILITY_NAMESPACE(
            """
   Bundle.entry.resource.ofType(MessageHeader).destination.receiver.resolve().identifier.where(
   extension.url = 'https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2Field' and
   extension.value = 'HD.1'
   ).value
   """),
    RECEIVING_FACILITY_UNIVERSAL_ID(
            """
   Bundle.entry.resource.ofType(MessageHeader).destination.receiver.resolve().identifier.where(
   extension.url = 'https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2Field' and
   extension.value = 'HD.2,HD.3'
   ).value
   """),
    RECEIVING_FACILITY_UNIVERSAL_ID_TYPE(
            """
   Bundle.entry.resource.ofType(MessageHeader).destination.receiver.resolve().identifier.where(
   extension.url = 'https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2Field' and
   extension.value = 'HD.2,HD.3'
   ).type.coding.code
   """),
    RECEIVING_APPLICATION_NAMESPACE(
            """
 Bundle.entry.resource.ofType(MessageHeader).destination.name
 """),
    RECEIVING_APPLICATION_UNIVERSAL_ID(
            """
 Bundle.entry.resource.ofType(MessageHeader).destination.extension.where(url = 'https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id').value
 """),
    RECEIVING_APPLICATION_UNIVERSAL_ID_TYPE(
            """
 Bundle.entry.resource.ofType(MessageHeader).destination.extension.where(url = 'https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id-type').value
 """);

    private final String path;

    FhirPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
