package gov.hhs.cdc.trustedintermediary.external.hapi

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.wrappers.HapiFhir
import spock.lang.Specification

class HapiMessageHelperTest extends Specification {

    def fhirEngineMock = Mock(HapiFhir)

    void setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(HapiFhir.class, fhirEngineMock)
        TestApplicationContext.register(HapiMessageHelper.class, HapiMessageHelper.getInstance())
        TestApplicationContext.injectRegisteredImplementations()
    }

    def "extractPlacerOrderNumber works"() {
    }

    def "extractSendingApplicationNamespace works"() {
    }

    def "extractSendingApplicationUniversalId works"() {
    }

    def "extractSendingApplicationUniversalIdType works"() {
    }

    def "extractSendingFacilityNamespace works"() {
    }

    def "extractSendingFacilityUniversalId works"() {
    }

    def "extractSendingFacilityUniversalIdType works"() {
    }

    def "extractReceivingApplicationNamespace works"() {
    }

    def "extractReceivingApplicationUniversalId works"() {
    }

    def "extractReceivingApplicationUniversalIdType works"() {
    }

    def "extractReceivingFacilityNamespace works"() {
    }

    def "extractReceivingFacilityUniversalId works"() {
    }

    def "extractReceivingFacilityUniversalIdType works"() {
    }
}
