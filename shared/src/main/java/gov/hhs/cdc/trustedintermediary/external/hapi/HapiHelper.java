package gov.hhs.cdc.trustedintermediary.external.hapi;

import java.util.Collections;
import java.util.stream.Stream;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.MessageHeader;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Reference;
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
        var messageHeader = findOrCreateMessageHeader(messageBundle);
        var meta = messageHeader.hasMeta() ? messageHeader.getMeta() : new Meta();

        if (meta.getTag(system, code) == null) {
            meta.addTag(new Coding(system, code, display));
        }

        messageHeader.setMeta(meta);
    }

    public static void setMessageTypeCoding(Bundle order, Coding coding) {
        var messageHeader = findOrCreateMessageHeader(order);
        messageHeader.setEvent(coding);
    }

    public static MessageHeader findOrCreateMessageHeader(Bundle bundle) {
        var messageHeader = resourceInBundle(bundle, MessageHeader.class);
        if (messageHeader == null) {
            messageHeader = new MessageHeader();
            bundle.addEntry(new Bundle.BundleEntryComponent().setResource(messageHeader));
        }
        return (MessageHeader) messageHeader;
    }

    public static void addSendingFacilityToMessageHeader(Bundle bundle, String name) {
        var header = findOrCreateMessageHeader(bundle);
        var org = new Organization();
        org.setName(name);
        header.setSender(new Reference(org));
    }

    public static void addReceivingFacilityToMessageHeader(Bundle bundle, String name) {
        var header = findOrCreateMessageHeader(bundle);
        var org = new Organization();
        org.setName(name);
        var destination = new MessageHeader.MessageDestinationComponent();
        destination.setReceiver(new Reference(org));
        header.setDestination(Collections.singletonList(destination));
        bundle.addEntry(new Bundle.BundleEntryComponent().setResource(org));
    }
}
