package gov.hhs.cdc.trustedintermediary.external.hapi;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Stream;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.MessageHeader;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.StringType;

/** Helper class that works on HapiFHIR constructs. */
public class HapiHelper {

    private HapiHelper() {}

    public static final String UNIVERSAL_ID_URL =
            "https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id";
    public static final String UNIVERSAL_ID_TYPE_URL =
            "https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id-type";

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

    // MSH - Message Header
    public static MessageHeader getMessageHeader(Bundle bundle) throws NoSuchElementException {
        MessageHeader messageHeader = (MessageHeader) resourceInBundle(bundle, MessageHeader.class);
        if (messageHeader == null) {
            throw new NoSuchElementException("MessageHeader not found in the bundle");
        }
        return messageHeader;
    }

    public static MessageHeader getOrCreateMessageHeader(Bundle bundle) {
        MessageHeader messageHeader = (MessageHeader) resourceInBundle(bundle, MessageHeader.class);
        if (messageHeader == null) {
            messageHeader = new MessageHeader();
            bundle.addEntry(new Bundle.BundleEntryComponent().setResource(messageHeader));
        }
        return messageHeader;
    }

    public static void addMetaTag(
            Bundle messageBundle, String system, String code, String display) {
        MessageHeader messageHeader = getMessageHeader(messageBundle);
        var meta = messageHeader.hasMeta() ? messageHeader.getMeta() : new Meta();

        if (meta.getTag(system, code) == null) {
            meta.addTag(new Coding(system, code, display));
        }

        messageHeader.setMeta(meta);
    }

    // MSH.9 - Message Type
    public static Coding getMessageTypeCoding(Bundle bundle) {
        MessageHeader messageHeader = getMessageHeader(bundle);
        return messageHeader.getEventCoding();
    }

    public static void setMessageTypeCoding(Bundle bundle, Coding coding) {
        var messageHeader = getOrCreateMessageHeader(bundle);
        messageHeader.setEvent(coding);
    }

    // MSH.3 - Sending Application
    public static MessageHeader.MessageSourceComponent getSendingApplication(Bundle bundle) {
        MessageHeader messageHeader = getMessageHeader(bundle);
        return messageHeader.getSource();
    }

    public static void setSendingApplication(
            Bundle bundle, MessageHeader.MessageSourceComponent sendingApplication) {
        MessageHeader messageHeader = getMessageHeader(bundle);
        messageHeader.setSource(sendingApplication);
    }

    public static MessageHeader.MessageSourceComponent createSendingApplication() {
        MessageHeader.MessageSourceComponent source = new MessageHeader.MessageSourceComponent();
        source.setId(UUID.randomUUID().toString());
        return source;
    }

    // MSH.4 - Sending Facility
    public static Organization getSendingFacility(Bundle bundle) {
        MessageHeader messageHeader = getMessageHeader(bundle);
        return (Organization) messageHeader.getSender().getResource();
    }

    public static void setSendingFacility(Bundle bundle, Organization sendingFacility) {
        MessageHeader messageHeader = getMessageHeader(bundle);
        String organizationId = sendingFacility.getId();
        Reference organizationReference = new Reference("Organization/" + organizationId);
        organizationReference.setResource(sendingFacility);
        messageHeader.setSender(organizationReference);
        bundle.addEntry(new Bundle.BundleEntryComponent().setResource(sendingFacility));
    }

    public static Organization createFacilityOrganization() {
        Organization organization = new Organization();
        String organizationId = UUID.randomUUID().toString();
        organization.setId(organizationId);
        return organization;
    }

    // MSH.5 - Receiving Application
    public static MessageHeader.MessageDestinationComponent getReceivingApplication(Bundle bundle) {
        MessageHeader messageHeader = getMessageHeader(bundle);
        return messageHeader.getDestinationFirstRep();
    }

    public static void setReceivingApplication(
            Bundle bundle, MessageHeader.MessageDestinationComponent receivingApplication) {
        MessageHeader messageHeader = getMessageHeader(bundle);
        messageHeader.setDestination(List.of(receivingApplication));
    }

    public static MessageHeader.MessageDestinationComponent createReceivingApplication() {
        MessageHeader.MessageDestinationComponent destination =
                new MessageHeader.MessageDestinationComponent();
        destination.setId(UUID.randomUUID().toString());
        return destination;
    }

    // MSH.6 - Receiving Facility
    public static Organization getReceivingFacility(Bundle bundle) {
        MessageHeader messageHeader = getMessageHeader(bundle);
        return (Organization) messageHeader.getDestinationFirstRep().getReceiver().getResource();
    }

    public static void setReceivingFacility(Bundle bundle, Organization receivingFacility) {
        MessageHeader messageHeader = getMessageHeader(bundle);
        String organizationId = receivingFacility.getId();
        Reference organizationReference = new Reference("Organization/" + organizationId);
        organizationReference.setResource(receivingFacility);
        MessageHeader.MessageDestinationComponent destination =
                new MessageHeader.MessageDestinationComponent();
        destination.setReceiver(organizationReference);
        messageHeader.setDestination(List.of(destination));
        bundle.addEntry(new Bundle.BundleEntryComponent().setResource(receivingFacility));
    }

    // PID.3 - Patient Identifier List
    public static List<Identifier> getPatientIdentifierList(Bundle bundle) {
        Patient patient = (Patient) resourceInBundle(bundle, Patient.class);
        if (patient == null) {
            throw new NoSuchElementException("Patient not found in the bundle");
        }
        return patient.getIdentifier();
    }

    // HD.1 - Namespace Id
    public static Identifier createHDNamespaceIdentifier() {
        Identifier identifier = new Identifier();
        Extension extension =
                new Extension(
                        "https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2Field",
                        new StringType("HD.1"));
        identifier.addExtension(extension);
        return identifier;
    }
}
