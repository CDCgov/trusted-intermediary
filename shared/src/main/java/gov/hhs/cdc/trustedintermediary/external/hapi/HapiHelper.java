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
import org.hl7.fhir.r4.model.ServiceRequest;

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

    public static void switchPlacerOrderAndGroupNumbers(Bundle bundle) {
        //        Update ORC-2 with content from ORC-4 in the ORU result message.
        //                Replace ORC-2.1 with content of ORC-4.1.
        //                Replace ORC-2.2 with content of ORC-4.2
        //        Effectively, we're swapping ORC-2 for ORC-4 and vice versa
        //        OBR 2.1: identifier[0]. value
        //        OBR 2.2: identifier.extension[1].extension[0].valueString
        //        OBR 4.1: code.coding[0].code
        //        OBR 4.2: code.coding[0].display
        var serviceRequests = resourcesInBundle(bundle, ServiceRequest.class);

        serviceRequests.forEach(
                serviceRequest -> {
                    var twoPointOne = serviceRequest.getIdentifier().get(0);
                    var twoPointTwo =
                            serviceRequest
                                    .getIdentifier()
                                    .get(0)
                                    .getExtensionByUrl(
                                            "https://reportstream.cdc.gov/fhir/StructureDefinition/namespace-id");
                    var fourPointOne = serviceRequest.getCode().getCoding().get(0);
                    var fourPointTwo = serviceRequest.getCode().getCoding().get(0);

                    var valueHolder = twoPointOne.getValue();
                    twoPointOne.setValue(fourPointOne.getCode());
                    fourPointOne.setCode(valueHolder);

                    valueHolder = twoPointTwo.getValue().primitiveValue();
                    twoPointTwo.setValue(fourPointTwo.getDisplayElement());
                    fourPointTwo.setCode(valueHolder);
                });
    }
}
