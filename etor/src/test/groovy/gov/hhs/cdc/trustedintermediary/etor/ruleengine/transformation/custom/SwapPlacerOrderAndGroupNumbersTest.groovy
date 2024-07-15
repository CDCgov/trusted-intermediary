package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom

import gov.hhs.cdc.trustedintermediary.ExamplesHelper

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiFhirResource
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiHelper
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.ServiceRequest
import spock.lang.Specification

class SwapPlacerOrderAndGroupNumbersTest extends Specification {
    def transformClass

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.injectRegisteredImplementations()

        transformClass = new SwapPlacerOrderAndGroupNumbers()
    }

    def "swap ORC-2.1 with ORC-4.1 and ORC-2.2 with ORC-4.2"() {
        given:
        def fhirResource = ExamplesHelper.getExampleFhirResource("../MN/004_MN_ORU_R01_NBS_1_hl7_translation.fhir")
        def bundle = fhirResource.getUnderlyingResource() as Bundle

        def serviceRequest = HapiHelper.resourceInBundle(bundle, ServiceRequest)
        def orc2_1 = HapiHelper.getORC2_1Value(serviceRequest)
        def orc2_2 = HapiHelper.getORC2_2Value(serviceRequest)
        def orc4_1 = HapiHelper.getORC4_1Value(serviceRequest)
        def orc4_2 = HapiHelper.getORC4_2Value(serviceRequest)

        expect:
        orc2_1 == "423787478"
        orc2_2 == "EPIC"
        orc4_1 == null
        orc4_2 == null

        when:
        transformClass.transform(fhirResource, null)
        def actualOrc2_1 = HapiHelper.getORC2_1Value(serviceRequest)
        def actualOrc2_2 = HapiHelper.getORC2_2Value(serviceRequest)
        def actualOrc4_1 = HapiHelper.getORC4_1Value(serviceRequest)
        def actualOrc4_2 = HapiHelper.getORC4_2Value(serviceRequest)

        then:
        actualOrc2_1 == orc4_1
        actualOrc2_2 == orc4_2
        actualOrc4_1 == orc2_1
        actualOrc4_2 == orc2_2
    }

    def "don't throw exception if service request resource not present"() {
        given:
        def bundle = new Bundle()
        HapiHelper.createMSHMessageHeader(bundle)

        when:
        transformClass.transform(new HapiFhirResource(bundle), null)

        then:
        noExceptionThrown()
    }
}
