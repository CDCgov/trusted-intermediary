package gov.hhs.cdc.trustedintermediary.external.hapi;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
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
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.PractitionerRole;
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
    public static final String EXTENSION_XON_ORGANIZATION_URL =
            "https://reportstream.cdc.gov/fhir/StructureDefinition/xon-organization";
    public static final String EXTENSION_CX_IDENTIFIER_URL =
            "https://reportstream.cdc.gov/fhir/StructureDefinition/cx-identifier";
    public static final String EXTENSION_XON10_URL = "XON.10";
    public static final String EXTENSION_XPN7_URL = "XPN.7";
    public static final String EXTENSION_CX5_URL = "CX.5";
    public static final StringType EXTENSION_HD1_DATA_TYPE = new StringType("HD.1");
    public static final StringType EXTENSION_HD2_HD3_DATA_TYPE = new StringType("HD.2,HD.3");
    public static final StringType EXTENSION_ORC12_DATA_TYPE = new StringType("ORC.12");
    public static final StringType EXTENSION_ORC2_DATA_TYPE = new StringType("ORC.2");
    public static final StringType EXTENSION_ORC4_DATA_TYPE = new StringType("ORC.4");
    public static final StringType EXTENSION_OBR2_DATA_TYPE = new StringType("OBR.2");
    public static final StringType EXTENSION_OBR16_DATA_TYPE = new StringType("OBR.16");

    public static final String EXTENSION_CODING_SYSTEM =
            "https://reportstream.cdc.gov/fhir/StructureDefinition/cwe-coding-system";

    public static final String EXTENSION_CWE_CODING =
            "https://reportstream.cdc.gov/fhir/StructureDefinition/cwe-coding";
    public static final String EXTENSION_ALT_CODING = "alt-coding";

    public static final Coding OML_CODING =
            new Coding("http://terminology.hl7.org/CodeSystem/v2-0003", "O21", "OML^O21^OML_O21");

    public static final String EXTENSION_ORC_URL =
            "https://reportstream.cdc.gov/fhir/StructureDefinition/orc-common-order";
    public static final String EXTENSION_ORC12_URL = "orc-12-ordering-provider";

    public static final String EXTENSION_OBR_URL =
            "https://reportstream.cdc.gov/fhir/StructureDefinition/obr-observation-request";

    public static final String LOCAL_CODE_URL =
            "https://terminology.hl7.org/CodeSystem-v2-0396.html#v2-0396-99zzzorL";
    public static final String LOINC_URL = "http://loinc.org";

    public static final String LOINC_CODE = "LN";
    public static final String PLT_CODE = "PLT";
    public static final String LOCAL_CODE = "L";

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
        if (bundle == null || bundle.getEntry().isEmpty()) {
            return Stream.empty();
        }
        return bundle.getEntry().stream()
                .map(Bundle.BundleEntryComponent::getResource)
                .filter(resource -> resource.getClass().equals(resourceType))
                .map(resource -> ((T) resource));
    }

    public static <T extends Resource> T resourceInBundle(Bundle bundle, Class<T> resourceType) {
        return resourcesInBundle(bundle, resourceType).findFirst().orElse(null);
    }

    // MSH-10
    public static String getMessageControlId(Bundle bundle) {
        return bundle.getIdentifier().getValue();
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

    // MSH-6 - Receiving Facility
    public static Organization getMSH6Organization(Bundle bundle) {
        MessageHeader messageHeader = getMSHMessageHeader(bundle);
        if (messageHeader == null) {
            return null;
        }
        return (Organization) messageHeader.getDestinationFirstRep().getReceiver().getResource();
    }

    // MSH-6.1 - Namespace ID
    public static Identifier getMSH6_1Identifier(Bundle bundle) {
        Organization receivingFacility = getMSH6Organization(bundle);
        if (receivingFacility == null) {
            return null;
        }
        List<Identifier> identifiers = receivingFacility.getIdentifier();
        return getHD1Identifier(identifiers);
    }

    public static Identifier createMSH6_1Identifier() {
        Identifier identifier = new Identifier();
        setHD1Identifier(identifier);
        return identifier;
    }

    public static void setMSH6_1Value(Bundle bundle, String value) {
        Identifier identifier = getMSH6_1Identifier(bundle);
        if (identifier == null) {
            identifier = createMSH6_1Identifier();
            Organization receivingFacility = getMSH6Organization(bundle);
            if (receivingFacility == null) {
                return;
            }
            receivingFacility.addIdentifier(identifier);
        }
        identifier.setValue(value);
    }

    // MSH-6.2 - Universal ID
    public static void removeMSH6_2_and_3_Identifier(Bundle bundle) {
        Organization receivingFacility = getMSH6Organization(bundle);
        if (receivingFacility == null) {
            return;
        }
        List<Identifier> identifiers = receivingFacility.getIdentifier();
        removeHl7FieldIdentifier(identifiers, EXTENSION_HD2_HD3_DATA_TYPE);
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

    public static void removePID3_5Value(Identifier patientIdentifier) {
        if (patientIdentifier == null) {
            return;
        }

        if (patientIdentifier.hasExtension(HapiHelper.EXTENSION_CX_IDENTIFIER_URL)) {
            patientIdentifier
                    .getExtensionByUrl(HapiHelper.EXTENSION_CX_IDENTIFIER_URL)
                    .removeExtension(HapiHelper.EXTENSION_CX5_URL);
        }

        // The cx-identifier extension can be removed if it has no more sub-extensions
        if (patientIdentifier
                .getExtensionByUrl(HapiHelper.EXTENSION_CX_IDENTIFIER_URL)
                .getExtension()
                .isEmpty()) {
            patientIdentifier.removeExtension(HapiHelper.EXTENSION_CX_IDENTIFIER_URL);
        }

        // The PID-3.5 also appears in the type coding
        patientIdentifier.setType(null);
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

    public static void setPID5_7ExtensionValue(Bundle bundle, String value) {
        Extension extension = getPID5Extension(bundle);
        if (extension != null && extension.hasExtension(HapiHelper.EXTENSION_XPN7_URL)) {
            extension
                    .getExtensionByUrl(HapiHelper.EXTENSION_XPN7_URL)
                    .setValue(new StringType(value));
        }
    }

    // ORC - Common Order

    // Diagnostic Report
    public static DiagnosticReport getDiagnosticReport(Bundle bundle) {
        return resourceInBundle(bundle, DiagnosticReport.class);
    }

    public static ServiceRequest getServiceRequest(DiagnosticReport diagnosticReport) {
        return (ServiceRequest) diagnosticReport.getBasedOnFirstRep().getResource();
    }

    public static PractitionerRole getPractitionerRole(ServiceRequest serviceRequest) {
        return (PractitionerRole) serviceRequest.getRequester().getResource();
    }

    public static Practitioner getPractitioner(PractitionerRole practitionerRole) {
        return (Practitioner) practitionerRole.getPractitioner().getResource();
    }

    public static Organization getOrganization(PractitionerRole practitionerRole) {
        return (Organization) practitionerRole.getOrganization().getResource();
    }

    // ORC-2 - Placer Order Number
    public static List<Identifier> getORC2Identifiers(ServiceRequest serviceRequest) {
        List<Identifier> identifiers = serviceRequest.getIdentifier();
        return getHl7FieldIdentifiers(identifiers, EXTENSION_ORC2_DATA_TYPE);
    }

    // ORC-2.1 - Entity Identifier
    public static String getORC2_1Value(ServiceRequest serviceRequest) {
        List<Identifier> identifiers = getORC2Identifiers(serviceRequest);
        if (identifiers.isEmpty()) {
            return null;
        }
        return getEI1Value(identifiers.get(0));
    }

    public static void setORC2_1Value(ServiceRequest serviceRequest, String value) {
        List<Identifier> identifiers = getORC2Identifiers(serviceRequest);
        if (identifiers.isEmpty()) {
            return;
        }
        identifiers.forEach(identifier -> setEI1Value(identifier, value));
    }

    // ORC-2.2 - Namespace ID
    public static String getORC2_2Value(ServiceRequest serviceRequest) {
        List<Identifier> identifiers = getORC2Identifiers(serviceRequest);
        if (identifiers.isEmpty()) {
            return null;
        }
        return getEI2Value(identifiers.get(0));
    }

    public static void setORC2_2Value(ServiceRequest serviceRequest, String value) {
        List<Identifier> identifiers = getORC2Identifiers(serviceRequest);
        if (identifiers.isEmpty()) {
            return;
        }
        identifiers.forEach(identifier -> setEI2Value(identifier, value));
    }

    // ORC-4 - Placer Group Number
    public static List<Identifier> getORC4Identifiers(ServiceRequest serviceRequest) {
        List<Identifier> identifiers = serviceRequest.getIdentifier();
        return getHl7FieldIdentifiers(identifiers, EXTENSION_ORC4_DATA_TYPE);
    }

    public static Identifier createORC4Identifier() {
        Identifier identifier = new Identifier();
        setHl7FieldExtensionValue(identifier, EXTENSION_ORC4_DATA_TYPE);
        return identifier;
    }

    // ORC-4.1 - Entity Identifier
    public static String getORC4_1Value(ServiceRequest serviceRequest) {
        List<Identifier> identifiers = getORC4Identifiers(serviceRequest);
        if (identifiers.isEmpty()) {
            return null;
        }
        return getEI1Value(identifiers.get(0));
    }

    public static void setORC4_1Value(ServiceRequest serviceRequest, String value) {
        List<Identifier> identifiers = getORC4Identifiers(serviceRequest);
        if (identifiers.isEmpty()) {
            Identifier identifier = createORC4Identifier();
            identifiers.add(identifier);
            serviceRequest.addIdentifier(identifier);
        }
        identifiers.forEach(identifier -> setEI1Value(identifier, value));
    }

    // ORC-4.2 - Namespace ID
    public static String getORC4_2Value(ServiceRequest serviceRequest) {
        List<Identifier> identifiers = getORC4Identifiers(serviceRequest);
        if (identifiers.isEmpty()) {
            return null;
        }
        return getEI2Value(identifiers.get(0));
    }

    public static void setORC4_2Value(ServiceRequest serviceRequest, String value) {
        List<Identifier> identifiers = getORC4Identifiers(serviceRequest);
        if (identifiers.isEmpty()) {
            Identifier identifier = createORC4Identifier();
            identifiers.add(identifier);
            serviceRequest.addIdentifier(identifier);
        }
        identifiers.forEach(identifier -> setEI2Value(identifier, value));
    }

    // ORC-21 - Ordering Facility Name
    public static String getORC21Value(ServiceRequest serviceRequest) {
        PractitionerRole practitionerRole = getPractitionerRole(serviceRequest);
        if (practitionerRole == null) {
            return null;
        }
        Organization organization = getOrganization(practitionerRole);
        if (organization == null
                || !organization.hasExtension(EXTENSION_XON_ORGANIZATION_URL)
                || !organization
                        .getExtensionByUrl(EXTENSION_XON_ORGANIZATION_URL)
                        .hasExtension(EXTENSION_XON10_URL)) {
            return null;
        }
        Extension xonOrgExtension = organization.getExtensionByUrl(EXTENSION_XON_ORGANIZATION_URL);
        Extension orc21Extension = xonOrgExtension.getExtensionByUrl(EXTENSION_XON10_URL);
        return orc21Extension.getValue().primitiveValue();
    }

    // OBR - Observation Request

    // OBR-2 - Placer Order Number
    public static Identifier getOBR2Identifier(ServiceRequest serviceRequest) {
        if (!serviceRequest.hasExtension(EXTENSION_OBR_URL)
                || !serviceRequest
                        .getExtensionByUrl(EXTENSION_OBR_URL)
                        .hasExtension(EXTENSION_OBR2_DATA_TYPE.toString())) {
            return null;
        }
        var extension =
                serviceRequest
                        .getExtensionByUrl(EXTENSION_OBR_URL)
                        .getExtensionByUrl(EXTENSION_OBR2_DATA_TYPE.toString());
        return extension.castToIdentifier(extension.getValue());
    }

    // OBR-2.1 - Entity Identifier
    public static String getOBR2_1Value(ServiceRequest serviceRequest) {
        var identifier = getOBR2Identifier(serviceRequest);
        if (identifier == null) {
            return null;
        }
        return getEI1Value(identifier);
    }

    public static void setOBR2_1Value(ServiceRequest serviceRequest, String value) {
        var identifier = getOBR2Identifier(serviceRequest);
        if (identifier != null) {
            setEI1Value(identifier, value);
        }
    }

    // OBR-2.2 - Namespace ID
    public static String getOBR2_2Value(ServiceRequest serviceRequest) {
        var identifier = getOBR2Identifier(serviceRequest);
        if (identifier == null) {
            return null;
        }
        return getEI2Value(identifier);
    }

    public static void setOBR2_2Value(ServiceRequest serviceRequest, String value) {
        var identifier = getOBR2Identifier(serviceRequest);
        if (identifier != null) {
            setEI2Value(identifier, value);
        }
    }

    // OBR-4 - Universal Service Identifier

    // OBR-4.1 - Identifier
    public static String getOBR4_1Value(ServiceRequest serviceRequest) {
        CodeableConcept cc = serviceRequest.getCode();
        if (cc.getCoding().isEmpty()) {
            return null;
        }
        return getCWE1Value(cc.getCoding().get(0));
    }

    // OBR16 - Ordering Provider

    // OBR16 -
    public static void setOBR16WithPractitioner(
            Extension obr16Extension, PractitionerRole practitionerRole) {
        if (practitionerRole == null) {
            return;
        }
        obr16Extension.setValue(practitionerRole.getPractitioner());
    }

    /**
     * Ensures that the extension exists for a given serviceRequest. If the extension does not
     * exist, it will create it.
     */
    public static Extension ensureExtensionExists(
            ServiceRequest serviceRequest, String extensionUrl) {
        Extension extension = serviceRequest.getExtensionByUrl(extensionUrl);
        if (extension == null) {
            // If the extension does not exist, create it and add it to the ServiceRequest
            extension = new Extension(extensionUrl);
            serviceRequest.addExtension(extension);
        }

        return extension;
    }

    /**
     * Ensures that a sub-extension exists within a parent extension. If the sub-extension does not
     * exist, it will create it.
     */
    public static Extension ensureSubExtensionExists(
            Extension parentExtension, String subExtensionUrl) {
        Extension subExtension = parentExtension.getExtensionByUrl(subExtensionUrl);
        if (subExtension == null) {
            subExtension = new Extension(subExtensionUrl);
            parentExtension.addExtension(subExtension);
        }
        return subExtension;
    }

    // HD - Hierarchic Designator
    public static Identifier getHD1Identifier(List<Identifier> identifiers) {
        List<Identifier> hd1Identifiers =
                getHl7FieldIdentifiers(identifiers, EXTENSION_HD1_DATA_TYPE);
        if (hd1Identifiers.isEmpty()) {
            return null;
        }
        return hd1Identifiers.get(0);
    }

    public static void setHD1Identifier(Identifier identifier) {
        setHl7FieldExtensionValue(identifier, EXTENSION_HD1_DATA_TYPE);
    }

    // Coding resource
    public static Extension getCodingExtensionByUrl(Coding coding, String url) {
        return coding.getExtensionByUrl(url);
    }

    public static boolean hasCodingExtensionWithUrl(Coding coding, String url) {
        return coding.getExtensionByUrl(url) != null;
    }

    public static boolean hasCodingSystem(Coding coding) {
        return coding.getSystem() != null;
    }

    public static String getCodingSystem(Coding coding) {
        return coding.getSystem();
    }

    // CWE - Coded with Exceptions
    public static String getCWE1Value(Coding coding) {
        return coding.getCode();
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

    public static List<Identifier> getHl7FieldIdentifiers(
            List<Identifier> identifiers, StringType dataType) {
        return identifiers.stream()
                .filter(
                        identifier ->
                                identifier.hasExtension(EXTENSION_HL7_FIELD_URL)
                                        && identifier
                                                .getExtensionByUrl(EXTENSION_HL7_FIELD_URL)
                                                .getValue()
                                                .equalsDeep(dataType))
                .collect(Collectors.toList());
    }

    public static void setHl7FieldExtensionValue(Identifier identifier, StringType dataType) {
        if (!identifier.hasExtension(EXTENSION_HL7_FIELD_URL)) {
            identifier.addExtension().setUrl(EXTENSION_HL7_FIELD_URL);
        }
        identifier.getExtensionByUrl(EXTENSION_HL7_FIELD_URL).setValue(dataType);
    }

    public static void removeHl7FieldIdentifier(List<Identifier> identifiers, StringType dataType) {
        identifiers.removeIf(
                identifier ->
                        identifier.hasExtension(EXTENSION_HL7_FIELD_URL)
                                && identifier
                                        .getExtensionByUrl(EXTENSION_HL7_FIELD_URL)
                                        .getValue()
                                        .equalsDeep(dataType));
    }

    public static String urlForCodeType(String code) {
        return switch (code) {
            case HapiHelper.LOINC_CODE -> HapiHelper.LOINC_URL;
            case HapiHelper.PLT_CODE -> null;
            default -> HapiHelper.LOCAL_CODE_URL;
        };
    }

    /**
     * Check if a given Coding resource has a coding extension and coding system extension with the
     * specified type.
     *
     * @param coding the resource to check. Expected to be converted from an HL7 CWE format field.
     * @param codingExt Name of coding extension (e.g. "coding", "alt-coding")
     * @param codingSystemExt Name of coding system to look for (e.g. Local code "L", LOINC "LN"...)
     * @return True if the Coding is formatted correctly and has the expected code type, else false
     */
    public static boolean hasDefinedCoding(
            Coding coding, String codingExt, String codingSystemExt) {
        var codingExtMatch =
                hasMatchingCodingExtension(coding, HapiHelper.EXTENSION_CWE_CODING, codingExt);
        var codingSystemExtMatch =
                hasMatchingCodingExtension(
                        coding, HapiHelper.EXTENSION_CODING_SYSTEM, codingSystemExt);
        return codingExtMatch && codingSystemExtMatch;
    }

    private static boolean hasMatchingCodingExtension(
            Coding coding, String extensionUrl, String valueToMatch) {
        if (!HapiHelper.hasCodingExtensionWithUrl(coding, extensionUrl)) {
            return false;
        }

        var extensionValue =
                HapiHelper.getCodingExtensionByUrl(coding, extensionUrl).getValue().toString();
        return Objects.equals(valueToMatch, extensionValue);
    }

    /**
     * Check if an observation has a Coding resource with the given code, coding, and coding system
     *
     * @param codeToMatch The code to look for.
     * @param codingExtToMatch Name of coding extension (e.g. "coding", "alt-coding")
     * @param codingSystemToMatch Name of coding system to look for (e.g. Local code "L", LOINC
     *     "LN"...)
     * @return True if the Coding is present in the observation, else false
     */
    public static boolean hasMatchingCoding(
            Observation observation,
            String codeToMatch,
            String codingExtToMatch,
            String codingSystemToMatch) {
        for (Coding coding : observation.getCode().getCoding()) {
            if (Objects.equals(coding.getCode(), codeToMatch)
                    && hasDefinedCoding(coding, codingExtToMatch, codingSystemToMatch)) {
                return true;
            }
        }
        return false;
    }
}
