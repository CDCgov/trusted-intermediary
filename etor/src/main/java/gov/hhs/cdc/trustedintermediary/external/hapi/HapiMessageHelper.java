package gov.hhs.cdc.trustedintermediary.external.hapi;

import gov.hhs.cdc.trustedintermediary.wrappers.HapiFhir;
import javax.inject.Inject;
import org.hl7.fhir.r4.model.Bundle;

public class HapiMessageHelper {

    public static final String PLACER_ORDER_NUMBER =
            """
            Bundle.entry.resource.ofType(ServiceRequest).identifier.where(type.coding.code = 'PLAC').value
        """;

    public static final String SENDING_FACILITY_NAMESPACE =
            """
            Bundle.entry.resource.ofType(MessageHeader).sender.resolve().identifier.where(
			    extension.url = 'https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2Field' and
			    extension.value = 'HD.1'
		    ).value
        """;
    public static final String SENDING_FACILITY_UNIVERSAL_ID =
            """
            Bundle.entry.resource.ofType(MessageHeader).sender.resolve().identifier.where(
                extension.url = 'https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2Field' and
                extension.value = 'HD.2,HD.3'
            ).value
        """;
    public static final String SENDING_FACILITY_UNIVERSAL_ID_TYPE =
            """
            Bundle.entry.resource.ofType(MessageHeader).sender.resolve().identifier.where(
                extension.url = 'https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2Field' and
                extension.value = 'HD.2,HD.3'
            ).type.coding.code
        """;
    public static final String SENDING_APPLICATION_NAMESPACE =
            """
            Bundle.entry.resource.ofType(MessageHeader).source.extension.where(url = 'https://reportstream.cdc.gov/fhir/StructureDefinition/namespace-id').value
        """;
    public static final String SENDING_APPLICATION_UNIVERSAL_ID =
            """
            Bundle.entry.resource.ofType(MessageHeader).source.extension.where(url = 'https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id').value
        """;
    public static final String SENDING_APPLICATION_UNIVERSAL_ID_TYPE =
            """
            Bundle.entry.resource.ofType(MessageHeader).source.extension.where(url = 'https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id-type').value
        """;
    public static final String RECEIVING_FACILITY_NAMESPACE =
            """
            Bundle.entry.resource.ofType(MessageHeader).destination.receiver.resolve().identifier.where(
                extension.url = 'https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2Field' and
                extension.value = 'HD.1'
            ).value
        """;
    public static final String RECEIVING_FACILITY_UNIVERSAL_ID =
            """
            Bundle.entry.resource.ofType(MessageHeader).destination.receiver.resolve().identifier.where(
			    extension.url = 'https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2Field' and
			    extension.value = 'HD.2,HD.3'
		    ).value
        """;
    public static final String RECEIVING_FACILITY_UNIVERSAL_ID_TYPE =
            """
            Bundle.entry.resource.ofType(MessageHeader).destination.receiver.resolve().identifier.where(
			    extension.url = 'https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2Field' and
			    extension.value = 'HD.2,HD.3'
		    ).type.coding.code
        """;
    public static final String RECEIVING_APPLICATION_NAMESPACE =
            """
            Bundle.entry.resource.ofType(MessageHeader).destination.name
        """;
    public static final String RECEIVING_APPLICATION_UNIVERSAL_ID =
            """
            Bundle.entry.resource.ofType(MessageHeader).destination.extension.where(url = 'https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id').value
        """;
    public static final String RECEIVING_APPLICATION_UNIVERSAL_ID_TYPE =
            """
            Bundle.entry.resource.ofType(MessageHeader).destination.extension.where(url = 'https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id-type').value
        """;

    private static final HapiMessageHelper INSTANCE = new HapiMessageHelper();

    @Inject private HapiFhir fhirEngine;

    public static HapiMessageHelper getInstance() {
        return INSTANCE;
    }

    private HapiMessageHelper() {}

    public String extractPlacerOrderNumber(Bundle messageBundle) {
        return fhirEngine.getStringFromFhirPath(messageBundle, PLACER_ORDER_NUMBER);
    }

    public String extractSendingApplicationNamespace(Bundle messageBundle) {
        return fhirEngine.getStringFromFhirPath(messageBundle, SENDING_APPLICATION_NAMESPACE);
    }

    public String extractSendingApplicationUniversalId(Bundle messageBundle) {
        return fhirEngine.getStringFromFhirPath(messageBundle, SENDING_APPLICATION_UNIVERSAL_ID);
    }

    public String extractSendingApplicationUniversalIdType(Bundle messageBundle) {
        return fhirEngine.getStringFromFhirPath(
                messageBundle, SENDING_APPLICATION_UNIVERSAL_ID_TYPE);
    }

    public String extractSendingFacilityNamespace(Bundle messageBundle) {
        return fhirEngine.getStringFromFhirPath(messageBundle, SENDING_FACILITY_NAMESPACE);
    }

    public String extractSendingFacilityUniversalId(Bundle messageBundle) {
        return fhirEngine.getStringFromFhirPath(messageBundle, SENDING_FACILITY_UNIVERSAL_ID);
    }

    public String extractSendingFacilityUniversalIdType(Bundle messageBundle) {
        return fhirEngine.getStringFromFhirPath(messageBundle, SENDING_FACILITY_UNIVERSAL_ID_TYPE);
    }

    public String extractReceivingApplicationNamespace(Bundle messageBundle) {
        return fhirEngine.getStringFromFhirPath(messageBundle, RECEIVING_APPLICATION_NAMESPACE);
    }

    public String extractReceivingApplicationUniversalId(Bundle messageBundle) {
        return fhirEngine.getStringFromFhirPath(messageBundle, RECEIVING_APPLICATION_UNIVERSAL_ID);
    }

    public String extractReceivingApplicationUniversalIdType(Bundle messageBundle) {
        return fhirEngine.getStringFromFhirPath(
                messageBundle, RECEIVING_APPLICATION_UNIVERSAL_ID_TYPE);
    }

    public String extractReceivingFacilityNamespace(Bundle messageBundle) {
        return fhirEngine.getStringFromFhirPath(messageBundle, RECEIVING_FACILITY_NAMESPACE);
    }

    public String extractReceivingFacilityUniversalId(Bundle messageBundle) {
        return fhirEngine.getStringFromFhirPath(messageBundle, RECEIVING_FACILITY_UNIVERSAL_ID);
    }

    public String extractReceivingFacilityUniversalIdType(Bundle messageBundle) {
        return fhirEngine.getStringFromFhirPath(
                messageBundle, RECEIVING_FACILITY_UNIVERSAL_ID_TYPE);
    }
}
