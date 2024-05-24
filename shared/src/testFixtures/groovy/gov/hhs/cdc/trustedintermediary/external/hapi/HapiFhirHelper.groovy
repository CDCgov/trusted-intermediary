package gov.hhs.cdc.trustedintermediary.external.hapi

import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.CodeableConcept
import org.hl7.fhir.r4.model.Coding
import org.hl7.fhir.r4.model.DiagnosticReport
import org.hl7.fhir.r4.model.Extension
import org.hl7.fhir.r4.model.HumanName
import org.hl7.fhir.r4.model.Identifier
import org.hl7.fhir.r4.model.MessageHeader
import org.hl7.fhir.r4.model.Organization
import org.hl7.fhir.r4.model.Patient
import org.hl7.fhir.r4.model.Reference
import org.hl7.fhir.r4.model.ServiceRequest
import org.hl7.fhir.r4.model.StringType

class HapiFhirHelper {

    static Bundle createMessageBundle(Map params) {
        String messageTypeCode = params.messageTypeCode as String ?: "ORM_O01"
        Organization receiverOrganization = params.receiverOrganization as Organization ?: new Organization()
        MessageHeader messageHeader = params.messageType as MessageHeader ?: new MessageHeader()

        MessageHeader.MessageDestinationComponent destination = messageHeader.addDestination()
        String receiverOrganizationFullUrl = "Organization/" + receiverOrganization.getId()
        destination.setReceiver(new Reference(receiverOrganizationFullUrl))

        Coding eventCoding = new Coding()
        eventCoding.setSystem("http://terminology.hl7.org/CodeSystem/v2-0003")
        String[] parts = messageTypeCode.split("_")
        eventCoding.setCode(parts[1])
        eventCoding.setDisplay(String.format("%s^%s^%s", parts[0], parts[1], messageTypeCode))
        messageHeader.setEvent(eventCoding)

        Bundle bundle = new Bundle()
        bundle.setType(Bundle.BundleType.MESSAGE)
        bundle.addEntry().setResource(messageHeader)
        bundle.addEntry().setFullUrl(receiverOrganizationFullUrl).setResource(receiverOrganization)
        return bundle
    }

    // MSH-3 - Sending Application
    static MessageHeader.MessageSourceComponent getMSH3MessageSourceComponent(
            Bundle bundle) {
        MessageHeader messageHeader = HapiHelper.getMSHMessageHeader(bundle)
        return messageHeader.getSource()
    }

    static void setMSH3MessageSourceComponent(
            Bundle bundle, MessageHeader.MessageSourceComponent sendingApplication) {
        MessageHeader messageHeader = HapiHelper.getMSHMessageHeader(bundle)
        messageHeader.setSource(sendingApplication)
    }

    static MessageHeader.MessageSourceComponent createMSH3MessageSourceComponent() {
        MessageHeader.MessageSourceComponent source = new MessageHeader.MessageSourceComponent()
        source.setId(UUID.randomUUID().toString())
        return source
    }

    // MSH-4 - Sending Facility
    static void setMSH4Organization(Bundle bundle, Organization sendingFacility) {
        MessageHeader messageHeader = HapiHelper.getMSHMessageHeader(bundle)
        Reference organizationReference = createOrganizationReference(bundle, sendingFacility)
        messageHeader.setSender(organizationReference)
    }

    static void setMSH4_1Identifier(Bundle bundle, Identifier identifier) {
        Organization sendingFacility = HapiHelper.getMSH4Organization(bundle)
        if (sendingFacility == null) {
            return
        }
        setHD1Identifier(identifier)
        sendingFacility.addIdentifier(identifier)
    }

    // MSH-5 - Receiving Application
    static void setMSH5MessageDestinationComponent(
            Bundle bundle, MessageHeader.MessageDestinationComponent receivingApplication) {
        MessageHeader messageHeader = HapiHelper.getMSHMessageHeader(bundle)
        messageHeader.setDestination(List.of(receivingApplication))
    }

    // MSH-6 - Receiving Facility
    static Organization getMSH6Organization(Bundle bundle) {
        MessageHeader messageHeader = HapiHelper.getMSHMessageHeader(bundle)
        return (Organization) messageHeader.getDestinationFirstRep().getReceiver().getResource()
    }

    static void setMSH6Organization(Bundle bundle, Organization receivingFacility) {
        MessageHeader messageHeader = HapiHelper.getMSHMessageHeader(bundle)
        Reference organizationReference = createOrganizationReference(bundle, receivingFacility)
        MessageHeader.MessageDestinationComponent destination =
                new MessageHeader.MessageDestinationComponent()
        destination.setReceiver(organizationReference)
        messageHeader.setDestination(List.of(destination))
    }

    // PID - Patient
    static Patient createPIDPatient(Bundle bundle) {
        Patient patient = new Patient()
        bundle.addEntry(new Bundle.BundleEntryComponent().setResource(patient))
        return patient
    }

    // PID-3 - Patient Identifier List
    static void setPID3Identifier(Bundle bundle, Identifier identifier) {
        Patient patient = HapiHelper.getPIDPatient(bundle)
        if (patient == null) {
            return
        }
        patient.setIdentifier(List.of(identifier))
    }

    // PID-3.4 - Assigning Authority
    static void setPID3_4Identifier(Bundle bundle, Identifier identifier) {
        Identifier patientIdentifier = HapiHelper.getPID3Identifier(bundle)
        if (patientIdentifier == null) {
            return
        }
        Organization organization = new Organization()
        organization.setIdentifier(List.of(identifier))
        Reference orgReference = createOrganizationReference(bundle, organization)
        patientIdentifier.setAssigner(orgReference)
    }

    static String getPID3_4Value(Bundle bundle) {
        Identifier identifier = HapiHelper.getPID3_4Identifier(bundle)
        if (identifier == null) {
            return null
        }
        return identifier.getValue()
    }

    // PID-3.5 - Identifier Type Code
    static String getPID3_5Value(Bundle bundle) {
        Identifier identifier = HapiHelper.getPID3Identifier(bundle)
        if (identifier == null) {
            return null
        }
        return HapiHelper.getCX5Value(identifier)
    }

    // PID-5 - Patient Name
    static void setPID5Extension(Bundle bundle) {
        Patient patient = HapiHelper.getPIDPatient(bundle)
        if (patient == null) {
            return
        }
        HumanName name = patient.getNameFirstRep()
        name.addExtension(new Extension(HapiHelper.EXTENSION_XPN_HUMAN_NAME_URL))
    }

    // PID-5.7 - Name Type Code
    static String getPID5_7Value(Bundle bundle) {
        Extension extension = HapiHelper.getPID5Extension(bundle)
        if (extension == null || !extension.hasExtension(HapiHelper.EXTENSION_XPN7_URL)) {
            return null
        }
        return extension
                .getExtensionByUrl(HapiHelper.EXTENSION_XPN7_URL)
                .getValue()
                .primitiveValue()
    }

    static void setPID5_7Value(Bundle bundle, String value) {
        Extension pid5Extension = HapiHelper.getPID5Extension(bundle)
        if (pid5Extension == null) {
            return
        }
        Extension xpn7Extension = pid5Extension.getExtensionByUrl(HapiHelper.EXTENSION_XPN7_URL)
        if (xpn7Extension == null) {
            xpn7Extension = new Extension(HapiHelper.EXTENSION_XPN7_URL)
            pid5Extension.addExtension(xpn7Extension)
        }
        xpn7Extension.setValue(new StringType(value))
    }

    // HD - Hierarchic Designator
    static void setHD1Identifier(Identifier identifier) {
        identifier.addExtension(HapiHelper.EXTENSION_HL7_FIELD_URL, HapiHelper.EXTENSION_HD1_DATA_TYPE)
    }

    // ORC - Common Order

    static void setORC2Identifier(ServiceRequest serviceRequest, Identifier identifier) {
        identifier.addExtension(HapiHelper.EXTENSION_HL7_FIELD_URL, HapiHelper.EXTENSION_ORC2_DATA_TYPE)
        serviceRequest.addIdentifier(identifier)
    }

    static DiagnosticReport getDiagnosticReport(Bundle bundle) {
        return HapiHelper.resourceInBundle(bundle, DiagnosticReport.class)
    }

    static DiagnosticReport createDiagnosticReport(Bundle bundle) {
        DiagnosticReport diagnosticReport = new DiagnosticReport()
        bundle.addEntry(new Bundle.BundleEntryComponent().setResource(diagnosticReport))
        return diagnosticReport
    }

    static ServiceRequest getBasedOnServiceRequest(DiagnosticReport diagnosticReport) {
        return (ServiceRequest) diagnosticReport.getBasedOnFirstRep().getResource()
    }

    static ServiceRequest createBasedOnServiceRequest(DiagnosticReport diagnosticReport) {
        ServiceRequest serviceRequest = new ServiceRequest()
        diagnosticReport.setBasedOn(List.of(new Reference(serviceRequest)))
        return serviceRequest
    }

    static Organization createOrganization() {
        Organization organization = new Organization()
        String organizationId = UUID.randomUUID().toString()
        organization.setId(organizationId)
        return organization
    }

    static MessageHeader.MessageDestinationComponent createMessageDestinationComponent() {
        MessageHeader.MessageDestinationComponent destination =
                new MessageHeader.MessageDestinationComponent()
        destination.setId(UUID.randomUUID().toString())
        return destination
    }

    static Reference createOrganizationReference(
            Bundle bundle, Organization organization) {
        String organizationId = organization.getId()
        Reference organizationReference = new Reference("Organization/" + organizationId)
        organizationReference.setResource(organization)
        bundle.addEntry(new Bundle.BundleEntryComponent().setResource(organization))
        return organizationReference
    }
}
