package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom

import gov.hhs.cdc.trustedintermediary.ExamplesHelper
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.RuleExecutionException
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiFhirResource
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiHelper
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.ServiceRequest
import spock.lang.Specification

class ObrOverridesTest  extends Specification {
    def transformClass

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.injectRegisteredImplementations()

        transformClass = new ObrOverrides()
    }

    def "add override values when values already exist"() {
        given:
        def fhirResource = ExamplesHelper.getExampleFhirResource("../MN/004_MN_ORU_R01_NBS_1_hl7_translation.fhir")
        def bundle = fhirResource.getUnderlyingResource() as Bundle
        def result = getOrbSections(bundle)
        def obr4_3 = result[0].getValue().primitiveValue()
        def obr4_4 = result[1]

        expect:
        obr4_3 == "LN"
        obr4_4 == null

        when:
        transformClass.transform(fhirResource, null)
        def transformedResult = getOrbSections(bundle)
        def transformedObr4_3 = transformedResult[0].getValue().primitiveValue()
        def transformedObr4_4 = transformedResult[1]

        then:
        transformedObr4_3 == obr4_3
        transformedObr4_4 == obr4_4
    }

    def "add override values when values don't exist"() {
    }

    def "throw RuleExecutionException if ServiceRequest doesn't have identifiers"() {
        given:
        def bundle = new Bundle()
        bundle.addEntry(new Bundle.BundleEntryComponent().setResource(new ServiceRequest()))

        when:
        transformClass.transform(new HapiFhirResource(bundle), null)

        then:
        thrown(RuleExecutionException)
    }

    // Returns a list of values for the ORB sections that need checking in the following order:
    // [4.1, 4.3, 4.4]
    def getOrbSections(Bundle bundle) {
        def serviceRequest = HapiHelper.resourceInBundle(bundle, ServiceRequest.class)
        def serviceCoding = serviceRequest.getCode().getCoding().get(0)
        def codingSystem = serviceCoding.getExtensionByUrl(HapiHelper.EXTENSION_CODING_SYSTEM)
        def altId = serviceCoding.getExtensionByUrl(HapiHelper.EXTENSION_ALTERNATE_VALUE)

        return [
            codingSystem,
            altId
        ]
    }
}
