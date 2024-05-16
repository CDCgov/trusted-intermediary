package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom

import gov.hhs.cdc.trustedintermediary.etor.ruleengine.FhirResource
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.CustomFhirTransformation
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Coding
import org.hl7.fhir.r4.model.MessageHeader

class HappyPathMockClass implements CustomFhirTransformation {

    @Override
    public void transform(final FhirResource<?> resource, final Map<String, String> args) {
        Bundle bundle = (Bundle) resource.getUnderlyingResource()
        MessageHeader messageHeader = new MessageHeader()

        String messageTypeCode = "mock_code"
        Coding eventCoding = new Coding()
        eventCoding.setCode(messageTypeCode)
        eventCoding.setDisplay(String.format("mock^code^%s", messageTypeCode))
        eventCoding.setSystem("http://terminology.hl7.org/CodeSystem/v2-0003")
        messageHeader.setEvent(eventCoding)
        bundle.addEntry(new Bundle.BundleEntryComponent().setResource(messageHeader))
    }
}
