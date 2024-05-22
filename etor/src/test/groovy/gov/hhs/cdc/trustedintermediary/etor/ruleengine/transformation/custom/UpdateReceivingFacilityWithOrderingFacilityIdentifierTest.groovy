package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom

import gov.hhs.cdc.trustedintermediary.ExamplesHelper
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiFhirHelper
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiFhirResource
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiHelper
import org.hl7.fhir.r4.model.Bundle
import spock.lang.Specification

class UpdateReceivingFacilityWithOrderingFacilityIdentifierTest extends Specification {

    def transformClass

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.injectRegisteredImplementations()

        transformClass = new UpdateReceivingFacilityWithOrderingFacilityIdentifier()
    }

    def "update receiving facility with ordering facility identifier"() {
        given:
        def fhirResource = ExamplesHelper.getExampleFhirResource('../MN/004_MN_ORU_R01_NBS_1_hl7_translation.fhir')
        def bundle = fhirResource.getUnderlyingResource() as Bundle
        def diagnosticReport = HapiHelper.getDiagnosticReport(bundle)
        def serviceRequest = HapiHelper.getBasedOnServiceRequest(diagnosticReport)
        def orc21_10 = HapiHelper.getORC21Value(serviceRequest)

        expect:
        HapiFhirHelper.getMSH6_1Value(bundle) != orc21_10

        when:
        transformClass.transform(new HapiFhirResource(bundle), null)

        then:
        HapiFhirHelper.getMSH6_1Value(bundle) == orc21_10
    }


    def "don't modify the bundle when there's missing service request"() {
        given:
        def bundle = new Bundle()
        HapiHelper.createMSHMessageHeader(bundle)
        HapiFhirHelper.createDiagnosticReport(bundle)
        def bundleCopy = bundle.copy()

        when:
        transformClass.transform(new HapiFhirResource(bundle), null)

        then:
        bundle.equalsDeep(bundleCopy)
    }

    def "don't throw exception if receiving facility not in bundle"() {
        given:
        def bundle = new Bundle()
        HapiHelper.createMSHMessageHeader(bundle)
        def bundleCopy = bundle.copy()

        when:
        transformClass.transform(new HapiFhirResource(bundle), null)

        then:
        noExceptionThrown()
        bundle.equalsDeep(bundleCopy)
    }
}
