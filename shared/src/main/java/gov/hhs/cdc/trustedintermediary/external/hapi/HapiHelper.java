package gov.hhs.cdc.trustedintermediary.external.hapi;

import java.util.List;
import java.util.stream.Stream;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.MessageHeader;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Patient;
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
    public static final String EXTENSION_CX_IDENTIFIER_URL =
            "https://reportstream.cdc.gov/fhir/StructureDefinition/cx-identifier";
    public static final String EXTENSION_XPN7_URL = "XPN.7";
    public static final String EXTENSION_CX5_URL = "CX.5";
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

        if (messageHeader == null) {
            messageHeader = createMSHMessageHeader(messageBundle);
        }

        var meta = messageHeader.hasMeta() ? messageHeader.getMeta() : new Meta();

        if (meta.getTag(system, code) == null) {
            meta.addTag(new Coding(system, code, display));
        }

        messageHeader.setMeta(meta);
    }

    // MSH-4 - Sending Facility
    public static Organization getMSH4Organization(Bundle bundle) {
        MessageHeader messageHeader = getMSHMessageHeader(bundle);
        if (messageHeader == null) {
            return null;
        }
        return (Organization) messageHeader.getSender().getResource();
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
        if (messageHeader == null) {
            return null;
        }
        return messageHeader.getDestinationFirstRep();
    }

    // MSH-9 - Message Type
    public static Coding getMSH9Coding(Bundle bundle) {
        MessageHeader messageHeader = getMSHMessageHeader(bundle);
        if (messageHeader == null) {
            return null;
        }
        return messageHeader.getEventCoding();
    }

    public static void setMSH9Coding(Bundle bundle, Coding coding) {
        var messageHeader = getMSHMessageHeader(bundle);
        if (messageHeader == null) {
            return;
        }
        messageHeader.setEvent(coding);
    }

    // MSH-9.3 - Message Structure
    public static String getMSH9_3Value(Bundle bundle) {
        Coding coding = getMSH9Coding(bundle);
        if (coding == null) {
            return null;
        }
        return coding.getDisplay();
    }

    public static void setMSH9_3Value(Bundle bundle, String value) {
        Coding coding = getMSH9Coding(bundle);
        if (coding == null) {
            return;
        }
        coding.setDisplay(value);
    }

    // PID - Patient
    public static Patient getPIDPatient(Bundle bundle) {
        return resourceInBundle(bundle, Patient.class);
    }

    // PID-3 - Patient Identifier List
    public static Identifier getPID3Identifier(Bundle bundle) {
        Patient patient = getPIDPatient(bundle);
        if (patient == null) {
            return null;
        }
        return patient.getIdentifierFirstRep();
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

    public static void setPID3_4Value(Bundle bundle, String value) {
        Identifier identifier = getPID3_4Identifier(bundle);
        if (identifier == null) {
            return;
        }
        identifier.setValue(value);
    }

    // PID-3.5 - Identifier Type Code
    public static void setPID3_5Value(Bundle bundle, String value) {
        Identifier identifier = getPID3Identifier(bundle);
        if (identifier == null) {
            return;
        }
        setCX5Value(identifier, value);
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

    public static void removePID5_7Extension(Bundle bundle) {
        Extension extension = getPID5Extension(bundle);
        if (extension != null && extension.hasExtension(HapiHelper.EXTENSION_XPN7_URL)) {
            extension.removeExtension(HapiHelper.EXTENSION_XPN7_URL);
        }
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
        if (!identifier.hasExtension(EXTENSION_ASSIGNING_AUTHORITY_URL)) {
            return null;
        }
        return identifier
                .getExtensionByUrl(EXTENSION_ASSIGNING_AUTHORITY_URL)
                .getExtensionByUrl(EXTENSION_NAMESPACE_ID_URL)
                .getValue()
                .primitiveValue();
    }

    public static void setEI2Value(Identifier identifier, String value) {
        if (!identifier.hasExtension(EXTENSION_ASSIGNING_AUTHORITY_URL)) {
            identifier.addExtension().setUrl(EXTENSION_ASSIGNING_AUTHORITY_URL);
        }
        if (!identifier
                .getExtensionByUrl(EXTENSION_ASSIGNING_AUTHORITY_URL)
                .hasExtension(EXTENSION_NAMESPACE_ID_URL)) {
            identifier
                    .getExtensionByUrl(EXTENSION_ASSIGNING_AUTHORITY_URL)
                    .addExtension()
                    .setUrl(EXTENSION_NAMESPACE_ID_URL);
        }
        identifier
                .getExtensionByUrl(EXTENSION_ASSIGNING_AUTHORITY_URL)
                .getExtensionByUrl(EXTENSION_NAMESPACE_ID_URL)
                .setValue(new StringType(value));
    }

    public static String getCX5Value(Identifier identifier) {
        if (!identifier.hasExtension(EXTENSION_CX_IDENTIFIER_URL)
                || !identifier
                        .getExtensionByUrl(EXTENSION_CX_IDENTIFIER_URL)
                        .hasExtension(EXTENSION_CX5_URL)) {
            return null;
        }
        return identifier
                .getExtensionByUrl(EXTENSION_CX_IDENTIFIER_URL)
                .getExtensionByUrl(EXTENSION_CX5_URL)
                .getValue()
                .primitiveValue();
    }

    public static void setCX5Value(Identifier identifier, String value) {
        if (!identifier.hasExtension(EXTENSION_CX_IDENTIFIER_URL)) {
            identifier.addExtension().setUrl(EXTENSION_CX_IDENTIFIER_URL);
        }
        if (!identifier
                .getExtensionByUrl(EXTENSION_CX_IDENTIFIER_URL)
                .hasExtension(EXTENSION_CX5_URL)) {
            identifier
                    .getExtensionByUrl(EXTENSION_CX_IDENTIFIER_URL)
                    .addExtension()
                    .setUrl(EXTENSION_CX5_URL);
        }
        identifier
                .getExtensionByUrl(EXTENSION_CX_IDENTIFIER_URL)
                .getExtensionByUrl(EXTENSION_CX5_URL)
                .setValue(new StringType(value));
    }

    static Identifier getHl7FieldIdentifier(List<Identifier> identifiers, StringType dataType) {
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
