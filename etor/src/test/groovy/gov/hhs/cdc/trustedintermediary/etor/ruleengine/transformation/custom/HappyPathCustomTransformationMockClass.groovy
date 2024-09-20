package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom

import gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.CustomFhirTransformation
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiHelper
import gov.hhs.cdc.trustedintermediary.wrappers.HealthData
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Coding

class HappyPathCustomTransformationMockClass implements CustomFhirTransformation {

    @Override
    public void transform(final HealthData<?> data, final Map<String, String> args) {
        Bundle bundle = (Bundle) data.getUnderlyingData()

        def system = "http://terminology.hl7.org/CodeSystem/v2-0003"
        def code = "mock_code"
        def display = String.format("mock^code^%s", code)

        Coding eventCoding = new Coding(system, code, display)
        HapiHelper.setMSH9Coding(bundle, eventCoding)
    }
}
