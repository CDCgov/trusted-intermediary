package gov.hhs.cdc.trustedintermediary.external.hapi;

import gov.hhs.cdc.trustedintermediary.context.ApplicationContext;
import gov.hhs.cdc.trustedintermediary.wrappers.HapiFhir;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.MessageHeader;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.StringType;

public class HapiMessageHelper {

    private final Map<String, String> fhirPaths;
    private static final HapiMessageHelper INSTANCE = new HapiMessageHelper();
    private String PLACER_ORDER_NUMBER_FHIR_PATH =
            "Bundle.entry.resource.ofType(ServiceRequest).identifier.where(type.coding.code = 'PLAC').value";

    private final HapiFhir fhirEngine = ApplicationContext.getImplementation(HapiFhir.class);

    public static HapiMessageHelper getInstance() {
        return INSTANCE;
    }

    private HapiMessageHelper() {
        this.fhirPaths = Collections.unmodifiableMap(loadFhirPaths());
    }

    public String extractPlacerOrderNumber() {
        return null;
    }

    public String extractSendingFacilityDetails(Bundle messageBundle) {
        String organizationReference =
                HapiHelper.resourcesInBundle(messageBundle, MessageHeader.class)
                        .map(MessageHeader::getSender)
                        .filter(Objects::nonNull)
                        .map(Reference::getReference)
                        .filter(Objects::nonNull)
                        .findFirst()
                        .orElse(null);

        if (organizationReference == null || organizationReference.isEmpty()) {
            return "";
        }

        // Extract from Organization/{id}
        String orgId =
                organizationReference.contains("/")
                        ? organizationReference.split("/")[1]
                        : organizationReference;

        return HapiHelper.resourcesInBundle(messageBundle, Organization.class)
                .filter(org -> orgId.equals(org.getIdElement().getIdPart()))
                .findFirst()
                .map(
                        org -> {
                            String facilityName = "", identifierValue = "", typeCode = "";

                            for (Identifier identifier : org.getIdentifier()) {
                                String extensionValue =
                                        identifier.getExtension().stream()
                                                .filter(
                                                        ext ->
                                                                "https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2Field"
                                                                        .equals(ext.getUrl()))
                                                .findFirst()
                                                .map(
                                                        ext ->
                                                                ((StringType) ext.getValue())
                                                                        .getValue())
                                                .orElse("");

                                // HD.1: namespace id
                                if ("HD.1".equals(extensionValue)) {
                                    facilityName = identifier.getValue();
                                } else if ("HD.2,HD.3".equals(extensionValue)) {
                                    identifierValue = identifier.getValue();
                                    // HD.2: universal Id, HD.3: universal id type
                                    typeCode =
                                            identifier.getType() != null
                                                            && !identifier
                                                                    .getType()
                                                                    .getCoding()
                                                                    .isEmpty()
                                                    ? identifier
                                                            .getType()
                                                            .getCoding()
                                                            .get(0)
                                                            .getCode()
                                                    : "";
                                }
                            }

                            return concatenateWithCaret(facilityName, identifierValue, typeCode);
                        })
                .orElse("");
    }

    public String extractReceivingApplicationDetails(Bundle messageBundle) {
        return HapiHelper.resourcesInBundle(messageBundle, MessageHeader.class)
                .filter(Objects::nonNull)
                .findFirst()
                .flatMap(
                        messageHeader ->
                                messageHeader.getDestination().stream()
                                        .filter(Objects::nonNull)
                                        .findFirst())
                .map(
                        destination -> {
                            String name = Objects.toString(destination.getName(), "");
                            String universalId =
                                    destination.getExtension().stream()
                                            .filter(
                                                    ext ->
                                                            "https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id"
                                                                    .equals(ext.getUrl()))
                                            .findFirst()
                                            .map(
                                                    ext ->
                                                            Objects.toString(
                                                                    ext.getValue().primitiveValue(),
                                                                    ""))
                                            .orElse("");
                            String universalIdType =
                                    destination.getExtension().stream()
                                            .filter(
                                                    ext ->
                                                            "https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id-type"
                                                                    .equals(ext.getUrl()))
                                            .findFirst()
                                            .map(
                                                    ext ->
                                                            Objects.toString(
                                                                    ext.getValue().primitiveValue(),
                                                                    ""))
                                            .orElse("");

                            return concatenateWithCaret(name, universalId, universalIdType);
                        })
                .orElse("");
    }

    private String concatenateWithCaret(String... values) {
        return Arrays.stream(values)
                .filter(Objects::nonNull) // Ensure null values are ignored
                .collect(Collectors.joining("^"));
    }

    private Map<String, String> loadFhirPaths() {
        Map<String, String> tempPaths = new HashMap<>();
        tempPaths.put(
                "receivingFacilityNamespace",
                """
            Bundle.entry.resource.ofType(MessageHeader).destination.receiver.resolve().identifier.where(
                extension.url = 'https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2Field' and
                extension.valueString = 'HD.1'
            ).value""");
        tempPaths.put(
                "receivingFacilityUniversalId",
                """
            Bundle.entry.resource.ofType(MessageHeader).destination.receiver.resolve().identifier.where(
                extension.url = 'https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2Field' and
                extension.valueString = 'HD.2,HD.3'
            ).value""");
        tempPaths.put(
                "receivingFacilityUniversalIdType",
                """
            Bundle.entry.resource.ofType(MessageHeader).destination.receiver.resolve().identifier.where(
                extension.url = 'https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2Field' and
                extension.valueString = 'HD.2,HD.3'
            ).type.coding.code""");
        tempPaths.put(
                "sendingFacilityNamespace",
                """
    Bundle.entry.resource.ofType(MessageHeader).sender.resolve().identifier.where(
        extension.url = 'https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2Field' and
        extension.valueString = 'HD.1'
    ).value
    """);
        tempPaths.put(
                "sendingFacilityUniversalId",
                """
            Bundle.entry.resource.ofType(MessageHeader).sender.resolve().identifier.where(
                extension.url = 'https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2Field' and
                extension.valueString = 'HD.2,HD.3'
            ).value""");
        tempPaths.put(
                "sendingFacilityUniversalIdType",
                """
            Bundle.entry.resource.ofType(MessageHeader).sender.resolve().identifier.where(
                extension.url = 'https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2Field' and
                extension.valueString = 'HD.2,HD.3'
            ).type.coding.code""");
        tempPaths.put(
                "sendingApplicationNamespace",
                """
            Bundle.entry.resource.ofType(MessageHeader).source.extension.where(url = 'https://reportstream.cdc.gov/fhir/StructureDefinition/namespace-id').valueString""");
        tempPaths.put(
                "sendingApplicationUniversalId",
                """
            Bundle.entry.resource.ofType(MessageHeader).source.extension.where(url = 'https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id').valueString""");
        tempPaths.put(
                "sendingApplicationUniversalIdType",
                """
            Bundle.entry.resource.ofType(MessageHeader).source.extension.where(url = 'https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id-type').valueString""");
        tempPaths.put(
                "receivingApplicationNamespace",
                """
            Bundle.entry.resource.ofType(MessageHeader).destination.extension.where(url = 'https://reportstream.cdc.gov/fhir/StructureDefinition/namespace-id').valueString""");
        tempPaths.put(
                "receivingApplicationUniversalId",
                """
            Bundle.entry.resource.ofType(MessageHeader).destination.extension.where(url = 'https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id').valueString""");
        tempPaths.put(
                "receivingApplicationUniversalIdType",
                """
            Bundle.entry.resource.ofType(MessageHeader).destination.extension.where(url = 'https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id-type').valueString""");
        // We need to add placer_order_number
        return tempPaths;
    }
}
