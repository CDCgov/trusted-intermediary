package gov.hhs.cdc.trustedintermediary

import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Coding
import org.hl7.fhir.r4.model.MessageHeader
import org.hl7.fhir.r4.model.Organization
import org.hl7.fhir.r4.model.Reference
import org.hl7.fhir.r4.model.Resource

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

    static resourceInBundle(Bundle bundle, Class resourceType) {
        return bundle.entry.stream()
                .map { it.resource }
                .filter { it.class == resourceType }
                .findFirst().orElse(null)
    }
}
