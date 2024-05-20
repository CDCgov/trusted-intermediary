package gov.hhs.cdc.trustedintermediary

import gov.hhs.cdc.trustedintermediary.external.hapi.HapiHelper
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Coding
import org.hl7.fhir.r4.model.Extension
import org.hl7.fhir.r4.model.Identifier
import org.hl7.fhir.r4.model.MessageHeader
import org.hl7.fhir.r4.model.Organization
import org.hl7.fhir.r4.model.Reference

class FhirBundleHelper {

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

    // MSH - Message Header
    static MessageHeader createMSHMessageHeader(Bundle bundle) {
        MessageHeader messageHeader = new MessageHeader()
        bundle.addEntry(new Bundle.BundleEntryComponent().setResource(messageHeader))
        return messageHeader
    }

    // MSH-5 - Receiving Application
    static void setMSH5MessageDestinationComponent(
            Bundle bundle, MessageHeader.MessageDestinationComponent receivingApplication) {
        MessageHeader messageHeader = HapiHelper.getMSHMessageHeader(bundle)
        messageHeader.setDestination(List.of(receivingApplication))
    }

    // PID-3.4 - Assigning Authority
    static String getPID3_4Value(Bundle bundle) {
        Identifier identifier = HapiHelper.getPID3_4Identifier(bundle)
        if (identifier == null) {
            return null
        }
        return identifier.getValue()
    }

    // PID-3.5 - Identifier Type Code
    static String getPID3_5Value(Bundle bundle) {
        Coding coding = HapiHelper.getPID3_5Coding(bundle)
        if (coding == null) {
            return null
        }
        return coding.getCode()
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

    static MessageHeader.MessageDestinationComponent createMessageDestinationComponent() {
        MessageHeader.MessageDestinationComponent destination =
                new MessageHeader.MessageDestinationComponent()
        destination.setId(UUID.randomUUID().toString())
        return destination
    }
}
