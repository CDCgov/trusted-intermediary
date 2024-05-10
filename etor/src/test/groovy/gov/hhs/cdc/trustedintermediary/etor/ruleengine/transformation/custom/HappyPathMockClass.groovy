package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom

import gov.hhs.cdc.trustedintermediary.etor.ruleengine.FhirResource
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.CustomFhirTransformation
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiHelper
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Coding
import org.hl7.fhir.r4.model.MessageHeader

class HappyPathMockClass implements CustomFhirTransformation {

    @Override
    public void transform(final FhirResource<?> resource, final Map<String, String> args) {
        Bundle bundle = (Bundle) resource.getUnderlyingResource()

        def system = "http://terminology.hl7.org/CodeSystem/v2-0003"
        def code = "mock_code"
        def display = String.format("mock^code^%s", code)

        Coding eventCoding = new Coding(system, code, display)
        HapiHelper.setMessageTypeCoding(bundle, eventCoding)
    }
}
