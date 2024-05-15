package gov.hhs.cdc.trustedintermediary.external.hapi;

import gov.hhs.cdc.trustedintermediary.context.ApplicationContext;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import java.util.AbstractMap;
import java.util.Optional;
import java.util.stream.Stream;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.MessageHeader;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;

/** Helper class that works on HapiFHIR constructs. */
public class HapiHelper {

    private HapiHelper() {}

    private static final Logger LOGGER = ApplicationContext.getImplementation(Logger.class);

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
     * Updates the value of an identifier for an Organization within a FHIR Bundle based on specific
     * criteria. This method processes each Patient resource within the bundle, identifies linked
     * Organizations via the assigner field of Patient identifiers, and checks each Organization's
     * identifier for specific extension criteria. If the criteria are met (specifically an
     * extension URL and a valueString that match predefined values), the identifier's value is
     * updated to a new specified value.
     *
     * <p>The following HL7 segment is changed: PID 3.4 - Assigning Authority
     *
     * @param bundle the FHIR Bundle containing Patient resources whose identifiers need to be
     *     updated.
     * @param newValue the new value to which the identifier type code should be set.
     */
    public static void updateOrganizationIdentifierValue(Bundle bundle, String newValue) {
        bundle.getEntry().stream()
                .map(Bundle.BundleEntryComponent::getResource)
                .filter(resource -> resource instanceof Patient)
                .map(resource -> (Patient) resource)
                .flatMap(patient -> patient.getIdentifier().stream())
                .map(
                        identifier ->
                                new AbstractMap.SimpleEntry<>(
                                        identifier,
                                        getOrganizationFromAssigner(
                                                bundle, identifier.getAssigner())))
                .filter(entry -> entry.getValue().isPresent())
                .flatMap(
                        entry ->
                                entry.getValue().get().getIdentifier().stream()
                                        .filter(
                                                orgIdentifier ->
                                                        hasRequiredExtension(
                                                                orgIdentifier,
                                                                "https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2Field",
                                                                "HD.1"))
                                        .peek(
                                                orgIdentifier ->
                                                        LOGGER.logInfo(
                                                                "Updating Organization identifier from: "
                                                                        + orgIdentifier.getValue()))
                                        .peek(orgIdentifier -> orgIdentifier.setValue(newValue)))
                .forEach(
                        orgIdentifier ->
                                LOGGER.logInfo(
                                        "Updated Organization identifier to: "
                                                + orgIdentifier.getValue()));
    }

    /**
     * Fetches the Organization referenced by a Patient identifier assigner.
     *
     * @param bundle The FHIR Bundle to search.
     * @param assigner The assigner reference.
     * @return Optional<Organization> if found, otherwise Optionale.empty().
     */
    private static Optional<Organization> getOrganizationFromAssigner(
            Bundle bundle, Reference assigner) {
        if (assigner == null || assigner.getReference() == null) {
            LOGGER.logInfo("Assigner or assigner reference is null.");
            return Optional.empty();
        }

        LOGGER.logInfo(
                "Starting search for Organization with reference: " + assigner.getReference());

        return bundle.getEntry().stream()
                .map(Bundle.BundleEntryComponent::getResource)
                .filter(resource -> resource instanceof Organization)
                .map(resource -> (Organization) resource)
                .peek(org -> LOGGER.logInfo("Checking organization with ID: " + org.getId()))
                .filter(
                        org -> {
                            boolean matches =
                                    ("Organization/" + org.getId()).equals(assigner.getReference());
                            LOGGER.logInfo(
                                    "Checking organization with ID: "
                                            + org.getId()
                                            + " for match: "
                                            + matches);
                            return matches;
                        })
                .findFirst();
    }

    /**
     * Checks if an identifier has the required extension with specific url and valueString.
     *
     * @param identifier The identifier to check.
     * @param url The extension url to match.
     * @param valueString The extension valueString to match.
     * @return true if the extension exists and matches, false otherwise.
     */
    private static boolean hasRequiredExtension(
            Identifier identifier, String url, String valueString) {
        return identifier.getExtension().stream()
                .anyMatch(
                        extension ->
                                url.equals(extension.getUrl())
                                        && valueString.equals(extension.getValue().toString()));
    }
}
