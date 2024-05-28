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
import org.hl7.fhir.r4.model.PractitionerRole
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
        Reference organizationReference = createOrganizationReference(sendingFacility)
        bundle.addEntry(new Bundle.BundleEntryComponent().setResource(sendingFacility))
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
    static void setMSH6Organization(Bundle bundle, Organization receivingFacility) {
        MessageHeader messageHeader = HapiHelper.getMSHMessageHeader(bundle)
        Reference organizationReference = createOrganizationReference(receivingFacility)
        MessageHeader.MessageDestinationComponent destination =
                new MessageHeader.MessageDestinationComponent()
        destination.setReceiver(organizationReference)
        bundle.addEntry(new Bundle.BundleEntryComponent().setResource(receivingFacility))
        messageHeader.setDestination(List.of(destination))
    }

    // MSH-6.1 - Namespace ID
    static void setMSH6_1Identifier(Bundle bundle, Identifier identifier) {
        Organization receivingFacility = HapiHelper.getMSH6Organization(bundle)
        if (receivingFacility == null) {
            return
        }
        setHD1Identifier(identifier)
        receivingFacility.addIdentifier(identifier)
    }

    static String getMSH6_1Value(Bundle bundle) {
        Identifier identifier = HapiHelper.getMSH6_1Identifier(bundle)
        if (identifier == null) {
            return null
        }
        return identifier.getValue()
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
        Organization organization = createOrganization()
        organization.setIdentifier(List.of(identifier))
        Reference orgReference = createOrganizationReference(organization)
        bundle.addEntry(new Bundle.BundleEntryComponent().setResource(organization))
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

    static DiagnosticReport createDiagnosticReport(Bundle bundle) {
        DiagnosticReport diagnosticReport = new DiagnosticReport()
        bundle.addEntry(new Bundle.BundleEntryComponent().setResource(diagnosticReport))
        return diagnosticReport
    }

    static ServiceRequest createBasedOnServiceRequest(DiagnosticReport diagnosticReport) {
        ServiceRequest serviceRequest = new ServiceRequest()
        diagnosticReport.setBasedOn(List.of(new Reference(serviceRequest)))
        return serviceRequest
    }

    // ORC-21 - Ordering Facility Name
    static void setORC21Value(ServiceRequest serviceRequest, String value) {
        PractitionerRole requester = createPractitionerRole()
        Organization organization = createOrganization()
        Reference organizationReference = createOrganizationReference(organization)
        requester.setOrganization(organizationReference)
        Reference requesterReference = createPractitionerRoleReference(requester)
        serviceRequest.setRequester(requesterReference)
        organization.addExtension().setUrl(HapiHelper.EXTENSION_XON_ORGANIZATION_URL).addExtension(HapiHelper.EXTENSION_XON10_URL, new StringType(value))
    }

    // OBR-4 - Universal Service Identifier
    static void setOBR4_1Value(ServiceRequest serviceRequest, String value) {
        serviceRequest.setCode(new CodeableConcept().addCoding(new Coding().setCode(value)))
    }

    static Organization createOrganization() {
        Organization organization = new Organization()
        String organizationId = UUID.randomUUID().toString()
        organization.setId(organizationId)
        return organization
    }

    static Reference createOrganizationReference(Organization organization) {
        String organizationId = organization.getId()
        Reference organizationReference = new Reference("Organization/" + organizationId)
        organizationReference.setResource(organization)
        return organizationReference
    }

    static PractitionerRole createPractitionerRole() {
        PractitionerRole practitionerRole = new PractitionerRole()
        String practitionerRoleId = UUID.randomUUID().toString()
        practitionerRole.setId(practitionerRoleId)
        return practitionerRole
    }

    static Reference createPractitionerRoleReference(PractitionerRole practitionerRole) {
        String organizationId = practitionerRole.getId()
        Reference organizationReference = new Reference("PractitionerRole/" + organizationId)
        organizationReference.setResource(practitionerRole)
        return organizationReference
    }

    static MessageHeader.MessageDestinationComponent createMessageDestinationComponent() {
        MessageHeader.MessageDestinationComponent destination =
                new MessageHeader.MessageDestinationComponent()
        destination.setId(UUID.randomUUID().toString())
        return destination
    }
}
