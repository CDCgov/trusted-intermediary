package gov.hhs.cdc.trustedintermediary.etor.results

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainRequest
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiMessageHelper
import gov.hhs.cdc.trustedintermediary.wrappers.FhirParseException
import gov.hhs.cdc.trustedintermediary.wrappers.HapiFhir
import gov.hhs.cdc.trustedintermediary.wrappers.MetricMetadata
import org.hl7.fhir.r4.model.Bundle
import spock.lang.Specification


class ResultControllerTest extends Specification {

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(ResultController, ResultController.getInstance())
        TestApplicationContext.register(MetricMetadata, Mock(MetricMetadata))
        TestApplicationContext.register(HapiMessageHelper, HapiMessageHelper.getInstance())
        TestApplicationContext.injectRegisteredImplementations()
    }

    def "parseResults Happy path works"() {
        given:
        def controller = ResultController.getInstance()
        def expectedBundle = new Bundle()

        def fhir = Mock(HapiFhir)
        fhir.parseResource(_ as String, _ as Class) >> expectedBundle
        TestApplicationContext.register(HapiFhir, fhir)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        def actualBundle = controller.parseResults(new DomainRequest()).getUnderlyingData()

        then:
        actualBundle == expectedBundle
    }

    def "parseResults throws an exception when unable to parse the request"() {
        given:
        def controller = ResultController.getInstance()
        def fhir = Mock(HapiFhir)
        fhir.parseResource(_ as String, _ as Class)  >> { throw new FhirParseException("ParseResult", new NullPointerException()) }
        TestApplicationContext.register(HapiFhir, fhir)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        controller.parseResults(new DomainRequest())

        then:
        thrown(FhirParseException)
    }
}
