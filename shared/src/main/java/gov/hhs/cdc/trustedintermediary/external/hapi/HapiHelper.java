package gov.hhs.cdc.trustedintermediary.external.hapi;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.MessageHeader;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.ServiceRequest;
import org.hl7.fhir.r4.model.StringType;

/** Helper class that works on HapiFHIR constructs. */
public class HapiHelper {

    private HapiHelper() {}

    public static final String EXTENSION_HL7_FIELD_URL =
            "https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2Field";
    public static final String EXTENSION_UNIVERSAL_ID_URL =
            "https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id";
    public static final String EXTENSION_UNIVERSAL_ID_TYPE_URL =
            "https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id-type";
    public static final String EXTENSION_ASSIGNING_AUTHORITY_URL =
            "https://reportstream.cdc.gov/fhir/StructureDefinition/assigning-authority";
    public static final String EXTENSION_NAMESPACE_ID_URL =
            "https://reportstream.cdc.gov/fhir/StructureDefinition/namespace-id";
    public static final String EXTENSION_XPN_HUMAN_NAME_URL =
            "https://reportstream.cdc.gov/fhir/StructureDefinition/xpn-human-name";
    public static final String EXTENSION_XPN7_URL = "XPN.7";
    public static final StringType EXTENSION_HD1_DATA_TYPE = new StringType("HD.1");
    public static final StringType EXTENSION_ORC2_DATA_TYPE = new StringType("ORC.2");
    public static final StringType EXTENSION_ORC4_DATA_TYPE = new StringType("ORC.4");

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

    public static <T extends Resource> T resourceInBundle(Bundle bundle, Class<T> resourceType) {
        return resourcesInBundle(bundle, resourceType).findFirst().orElse(null);
    }

    // MSH - Message Header
    public static MessageHeader getMSHMessageHeader(Bundle bundle) {
        return resourceInBundle(bundle, MessageHeader.class);
    }

    public static MessageHeader createMSHMessageHeader(Bundle bundle) {
        MessageHeader messageHeader = new MessageHeader();
        bundle.addEntry(new Bundle.BundleEntryComponent().setResource(messageHeader));
        return messageHeader;
    }

    public static void addMetaTag(
            Bundle messageBundle, String system, String code, String display) {
        MessageHeader messageHeader = getMSHMessageHeader(messageBundle);
        var meta = messageHeader.hasMeta() ? messageHeader.getMeta() : new Meta();

        if (meta.getTag(system, code) == null) {
            meta.addTag(new Coding(system, code, display));
        }

        messageHeader.setMeta(meta);
    }

    // MSH-3 - Sending Application
    public static MessageHeader.MessageSourceComponent getMSH3MessageSourceComponent(
            Bundle bundle) {
        MessageHeader messageHeader = getMSHMessageHeader(bundle);
        return messageHeader.getSource();
    }

    public static void setMSH3MessageSourceComponent(
            Bundle bundle, MessageHeader.MessageSourceComponent sendingApplication) {
        MessageHeader messageHeader = getMSHMessageHeader(bundle);
        messageHeader.setSource(sendingApplication);
    }

    public static MessageHeader.MessageSourceComponent createMSH3MessageSourceComponent() {
        MessageHeader.MessageSourceComponent source = new MessageHeader.MessageSourceComponent();
        source.setId(UUID.randomUUID().toString());
        return source;
    }

    // MSH-4 - Sending Facility
    public static Organization getMSH4Organization(Bundle bundle) {
        MessageHeader messageHeader = getMSHMessageHeader(bundle);
        return (Organization) messageHeader.getSender().getResource();
    }

    public static void setMSH4Organization(Bundle bundle, Organization sendingFacility) {
        MessageHeader messageHeader = getMSHMessageHeader(bundle);
        Reference organizationReference = createOrganizationReference(bundle, sendingFacility);
        messageHeader.setSender(organizationReference);
    }

    // MSH-4.1 - Namespace ID
    public static Identifier getMSH4_1Identifier(Bundle bundle) {
        Organization sendingFacility = getMSH4Organization(bundle);
        if (sendingFacility == null) {
            return null;
        }
        List<Identifier> identifiers = sendingFacility.getIdentifier();
        return getHD1Identifier(identifiers);
    }

    // MSH-5 - Receiving Application
    public static MessageHeader.MessageDestinationComponent getMSH5MessageDestinationComponent(
            Bundle bundle) {
        MessageHeader messageHeader = getMSHMessageHeader(bundle);
        return messageHeader.getDestinationFirstRep();
    }

    public static void setMSH5MessageDestinationComponent(
            Bundle bundle, MessageHeader.MessageDestinationComponent receivingApplication) {
        MessageHeader messageHeader = getMSHMessageHeader(bundle);
        messageHeader.setDestination(List.of(receivingApplication));
    }

    // MSH-6 - Receiving Facility
    public static Organization getMSH6Organization(Bundle bundle) {
        MessageHeader messageHeader = getMSHMessageHeader(bundle);
        return (Organization) messageHeader.getDestinationFirstRep().getReceiver().getResource();
    }

    public static void setMSH6Organization(Bundle bundle, Organization receivingFacility) {
        MessageHeader messageHeader = getMSHMessageHeader(bundle);
        Reference organizationReference = createOrganizationReference(bundle, receivingFacility);
        MessageHeader.MessageDestinationComponent destination =
                new MessageHeader.MessageDestinationComponent();
        destination.setReceiver(organizationReference);
        messageHeader.setDestination(List.of(destination));
    }

    // MSH-9 - Message Type
    public static Coding getMSH9Coding(Bundle bundle) {
        MessageHeader messageHeader = getMSHMessageHeader(bundle);
        return messageHeader.getEventCoding();
    }

    public static void setMSH9Coding(Bundle bundle, Coding coding) {
        var messageHeader = getMSHMessageHeader(bundle);
        messageHeader.setEvent(coding);
    }

    // MSH-9.3 - Message Structure
    public static String getMSH9_3Value(Bundle bundle) {
        Coding coding = getMSH9Coding(bundle);
        return coding.getDisplay();
    }

    public static void setMSH9_3Value(Bundle bundle, String value) {
        Coding coding = getMSH9Coding(bundle);
        coding.setDisplay(value);
    }

    // PID - Patient
    public static Patient getPIDPatient(Bundle bundle) {
        return resourceInBundle(bundle, Patient.class);
    }

    public static Patient createPIDPatient(Bundle bundle) {
        Patient patient = new Patient();
        bundle.addEntry(new Bundle.BundleEntryComponent().setResource(patient));
        return patient;
    }

    // PID-3 - Patient Identifier List
    public static Identifier getPID3Identifier(Bundle bundle) {
        Patient patient = getPIDPatient(bundle);
        if (patient == null) {
            return null;
        }
        return patient.getIdentifierFirstRep();
    }

    public static void setPID3Identifier(Bundle bundle, Identifier identifier) {
        Patient patient = getPIDPatient(bundle);
        if (patient == null) {
            return;
        }
        patient.setIdentifier(List.of(identifier));
    }

    // PID-3.4 - Assigning Authority
    public static Identifier getPID3_4Identifier(Bundle bundle) {
        Identifier identifier = getPID3Identifier(bundle);
        if (identifier == null) {
            return null;
        }
        Organization organization = (Organization) identifier.getAssigner().getResource();
        return organization.getIdentifierFirstRep();
    }

    public static void setPID3_4Identifier(Bundle bundle, Identifier identifier) {
        Identifier patientIdentifier = getPID3Identifier(bundle);
        if (patientIdentifier == null) {
            return;
        }
        Organization organization = new Organization();
        organization.setIdentifier(List.of(identifier));
        Reference orgReference = createOrganizationReference(bundle, organization);
        patientIdentifier.setAssigner(orgReference);
    }

    public static String getPID3_4Value(Bundle bundle) {
        Identifier identifier = getPID3_4Identifier(bundle);
        if (identifier == null) {
            return null;
        }
        return identifier.getValue();
    }

    public static void setPID3_4Value(Bundle bundle, String value) {
        Identifier identifier = getPID3_4Identifier(bundle);
        if (identifier == null) {
            return;
        }
        identifier.setValue(value);
    }

    // PID-3.5 - Identifier Type Code
    public static Coding getPID3_5Coding(Bundle bundle) {
        Identifier identifier = getPID3Identifier(bundle);
        if (identifier == null) {
            return null;
        }
        return identifier.getType().getCodingFirstRep();
    }

    public static void setPID3_5Coding(Bundle bundle, Coding coding) {
        Identifier identifier = getPID3Identifier(bundle);
        if (identifier == null) {
            return;
        }
        identifier.setType(new CodeableConcept().addCoding(coding));
    }

    public static String getPID3_5Value(Bundle bundle) {
        Coding coding = getPID3_5Coding(bundle);
        if (coding == null) {
            return null;
        }
        return coding.getCode();
    }

    public static void setPID3_5Value(Bundle bundle, String value) {
        Coding coding = getPID3_5Coding(bundle);
        if (coding == null) {
            return;
        }
        coding.setCode(value);
    }

    // PID-5 - Patient Name
    public static Extension getPID5Extension(Bundle bundle) {
        Patient patient = getPIDPatient(bundle);
        if (patient == null) {
            return null;
        }
        HumanName name = patient.getNameFirstRep();
        return name.getExtensionByUrl(HapiHelper.EXTENSION_XPN_HUMAN_NAME_URL);
    }

    public static void setPID5Extension(Bundle bundle) {
        Patient patient = getPIDPatient(bundle);
        if (patient == null) {
            return;
        }
        HumanName name = patient.getNameFirstRep();
        name.addExtension(new Extension(HapiHelper.EXTENSION_XPN_HUMAN_NAME_URL));
    }

    // PID-5.7 - Name Type Code
    public static String getPID5_7Value(Bundle bundle) {
        Extension extension = getPID5Extension(bundle);
        if (extension == null || !extension.hasExtension(HapiHelper.EXTENSION_XPN7_URL)) {
            return null;
        }
        return extension
                .getExtensionByUrl(HapiHelper.EXTENSION_XPN7_URL)
                .getValue()
                .primitiveValue();
    }

    public static void setPID5_7Value(Bundle bundle, String value) {
        Extension pid5Extension = getPID5Extension(bundle);
        if (pid5Extension == null) {
            return;
        }
        Extension xpn7Extension = pid5Extension.getExtensionByUrl(HapiHelper.EXTENSION_XPN7_URL);
        if (xpn7Extension == null) {
            xpn7Extension = new Extension(HapiHelper.EXTENSION_XPN7_URL);
            pid5Extension.addExtension(xpn7Extension);
        }
        xpn7Extension.setValue(new StringType(value));
    }

    public static void removePID5_7Extension(Bundle bundle) {
        Extension extension = getPID5Extension(bundle);
        if (extension != null && extension.hasExtension(HapiHelper.EXTENSION_XPN7_URL)) {
            extension.removeExtension(HapiHelper.EXTENSION_XPN7_URL);
        }
    }

    // ORC - Common Order
    public static DiagnosticReport getDiagnosticReport(Bundle bundle) {
        return resourceInBundle(bundle, DiagnosticReport.class);
    }

    public static DiagnosticReport createDiagnosticReport(Bundle bundle) {
        DiagnosticReport diagnosticReport = new DiagnosticReport();
        bundle.addEntry(new Bundle.BundleEntryComponent().setResource(diagnosticReport));
        return diagnosticReport;
    }

    public static ServiceRequest getBasedOnServiceRequest(DiagnosticReport diagnosticReport) {
        return (ServiceRequest) diagnosticReport.getBasedOnFirstRep().getResource();
    }

    public static ServiceRequest createBasedOnServiceRequest(DiagnosticReport diagnosticReport) {
        ServiceRequest serviceRequest = new ServiceRequest();
        diagnosticReport.setBasedOn(List.of(new Reference(serviceRequest)));
        return serviceRequest;
    }

    // ORC-2 - Placer Order Number
    public static Identifier getORC2Identifier(ServiceRequest serviceRequest) {
        List<Identifier> identifiers = serviceRequest.getIdentifier();
        return getHl7FieldIdentifier(identifiers, EXTENSION_ORC2_DATA_TYPE);
    }

    // ORC-2.1 - Entity Identifier
    public static String getORC2_1Value(ServiceRequest serviceRequest) {
        Identifier identifier = getORC2Identifier(serviceRequest);
        if (identifier == null) {
            return null;
        }
        return getEI1Value(identifier);
    }

    public static void setORC2_1Value(ServiceRequest serviceRequest, String value) {
        Identifier identifier = getORC2Identifier(serviceRequest);
        if (identifier == null) {
            return;
        }
        setEI1Value(identifier, value);
    }

    // ORC-2.2 - Namespace ID
    public static String getORC2_2Value(ServiceRequest serviceRequest) {
        Identifier identifier = getORC2Identifier(serviceRequest);
        if (identifier == null) {
            return null;
        }
        return getEI2Value(identifier);
    }

    public static void setORC2_2Value(ServiceRequest serviceRequest, String value) {
        Identifier identifier = getORC2Identifier(serviceRequest);
        if (identifier == null) {
            return;
        }
        setEI2Value(identifier, value);
    }

    // ORC-4 - Placer Group Number
    public static Coding getORC4Coding(ServiceRequest serviceRequest) {
        List<Coding> codings = serviceRequest.getCode().getCoding();
        if (codings.isEmpty()) {
            return null;
        }
        return codings.get(0);
    }

    public static void setORC4Coding(ServiceRequest serviceRequest, Coding coding) {
        serviceRequest.getCode().setCoding(List.of(coding));
    }

    // ORC-4.1 - Entity Identifier
    public static String getORC4_1Value(ServiceRequest serviceRequest) {
        Coding coding = getORC4Coding(serviceRequest);
        if (coding == null) {
            return null;
        }
        return coding.getCode();
    }

    public static void setORC4_1Value(ServiceRequest serviceRequest, String value) {
        Coding coding = getORC4Coding(serviceRequest);
        if (coding == null) {
            coding = new Coding();
            setORC4Coding(serviceRequest, coding);
        }
        coding.setCode(value);
    }

    // ORC-4.2 - Namespace ID
    public static String getORC4_2Value(ServiceRequest serviceRequest) {
        Coding coding = getORC4Coding(serviceRequest);
        if (coding == null) {
            return null;
        }
        return coding.getDisplay();
    }

    public static void setORC4_2Value(ServiceRequest serviceRequest, String value) {
        Coding coding = getORC4Coding(serviceRequest);
        if (coding == null) {
            coding = new Coding();
            setORC4Coding(serviceRequest, coding);
        }
        coding.setDisplay(value);
    }

    // HD - Hierarchic Designator
    public static Identifier getHD1Identifier(List<Identifier> identifiers) {
        return getHl7FieldIdentifier(identifiers, EXTENSION_HD1_DATA_TYPE);
    }

    // EI - Entity Identifier
    public static String getEI1Value(Identifier identifier) {
        return identifier.getValue();
    }

    public static void setEI1Value(Identifier identifier, String value) {
        identifier.setValue(value);
    }

    public static String getEI2Value(Identifier identifier) {
        return identifier
                .getExtensionByUrl(EXTENSION_ASSIGNING_AUTHORITY_URL)
                .getExtensionByUrl(EXTENSION_NAMESPACE_ID_URL)
                .getValue()
                .primitiveValue();
    }

    public static void setEI2Value(Identifier identifier, String value) {
        identifier
                .getExtensionByUrl(EXTENSION_ASSIGNING_AUTHORITY_URL)
                .getExtensionByUrl(EXTENSION_NAMESPACE_ID_URL)
                .setValue(new StringType(value));
    }

    protected static Organization createOrganization() {
        Organization organization = new Organization();
        String organizationId = UUID.randomUUID().toString();
        organization.setId(organizationId);
        return organization;
    }

    protected static MessageHeader.MessageDestinationComponent createMessageDestinationComponent() {
        MessageHeader.MessageDestinationComponent destination =
                new MessageHeader.MessageDestinationComponent();
        destination.setId(UUID.randomUUID().toString());
        return destination;
    }

    protected static Reference createOrganizationReference(
            Bundle bundle, Organization organization) {
        String organizationId = organization.getId();
        Reference organizationReference = new Reference("Organization/" + organizationId);
        organizationReference.setResource(organization);
        bundle.addEntry(new Bundle.BundleEntryComponent().setResource(organization));
        return organizationReference;
    }

    protected static Identifier getHl7FieldIdentifier(
            List<Identifier> identifiers, StringType dataType) {
        for (Identifier identifier : identifiers) {
            if (identifier.hasExtension(EXTENSION_HL7_FIELD_URL)
                    && identifier
                            .getExtensionByUrl(EXTENSION_HL7_FIELD_URL)
                            .getValue()
                            .equalsDeep(dataType)) {
                return identifier;
            }
        }
        return null;
    }
}
