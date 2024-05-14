package gov.hhs.cdc.trustedintermediary.external.hapi;

import java.util.List;
import java.util.stream.Stream;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.MessageHeader;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Resource;

/** Helper class that works on HapiFHIR constructs. */
public class HapiHelper {

    private HapiHelper() {}

    public static final Coding OML_CODING =
            new Coding(
                    "http://terminology.hl7.org/CodeSystem/v2-0003",
                    "O21",
                    "OML - Laboratory order");

    /**
     * Returns a {@link Stream} of FHIR resources inside the provided {@link Bundle} that match the
     * given resource type.
     *
     * @param bundle The bundle to search.
     * @param resourceType The class of the resource to search for.
     * @param <T> The type that either is or extends {@link Resource}.
     * @return The stream of the given resource type.
     */
    public static <T extends Resource> Stream<T> resourcesInBundle(
            Bundle bundle, Class<T> resourceType) {
        return bundle.getEntry().stream()
                .map(Bundle.BundleEntryComponent::getResource)
                .filter(resource -> resource.getClass().equals(resourceType))
                .map(resource -> ((T) resource));
    }

    public static <T extends Resource> Resource resourceInBundle(
            Bundle bundle, Class<T> resourceType) {
        return resourcesInBundle(bundle, resourceType).findFirst().orElse(null);
    }

    public static void addMetaTag(
            Bundle messageBundle, String system, String code, String display) {
        var messageHeader = findOrInitializeMessageHeader(messageBundle);
        var meta = messageHeader.hasMeta() ? messageHeader.getMeta() : new Meta();

        if (meta.getTag(system, code) == null) {
            meta.addTag(new Coding(system, code, display));
        }

        messageHeader.setMeta(meta);
    }

    public static void setMessageTypeCoding(Bundle order, Coding coding) {
        var messageHeader = findOrInitializeMessageHeader(order);
        messageHeader.setEvent(coding);
    }

    public static MessageHeader findOrInitializeMessageHeader(Bundle bundle) {
        var messageHeader = resourceInBundle(bundle, MessageHeader.class);
        if (messageHeader == null) {
            messageHeader = new MessageHeader();
            bundle.addEntry(new Bundle.BundleEntryComponent().setResource(messageHeader));
        }
        return (MessageHeader) messageHeader;
    }

    /**
     * Updates the code of an identifier's type within a Patient's identifier list in a FHIR Bundle
     * based on a specified type code. This method is intended to work specifically with identifier
     * types that correspond to HL7 PID segments PID 3.4 and PID 3.5.
     *
     * @param bundle the FHIR Bundle containing Patient resources whose identifiers need to be
     *     updated.
     * @param field the identifier type code to match on (corresponds to parts of PID 3.4 and PID
     *     3.5).
     * @param newValue the new value to which the identifier type code should be set.
     * @throws IllegalArgumentException if the provided bundle is null, or if any other argument
     *     does not meet the method's requirements.
     */
    public static void updatePatientIdentifierType(Bundle bundle, String field, String newValue) {
        bundle.getEntry()
                .forEach(
                        entry -> {
                            if (entry.getResource() instanceof Patient patient) {
                                List<Identifier> identifiers = patient.getIdentifier();
                                identifiers.stream()
                                        .filter(
                                                identifier ->
                                                        identifier.getType() != null
                                                                && identifier
                                                                                .getType()
                                                                                .getCodingFirstRep()
                                                                        != null
                                                                && field.equals(
                                                                        identifier
                                                                                .getType()
                                                                                .getCodingFirstRep()
                                                                                .getCode()))
                                        .forEach(
                                                identifier ->
                                                        identifier
                                                                .getType()
                                                                .getCodingFirstRep()
                                                                .setCode(newValue));
                            }
                        });
    }
}
