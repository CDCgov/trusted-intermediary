package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom

import gov.hhs.cdc.trustedintermediary.FhirBundleHelper
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiFhirResource
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiHelper
import gov.hhs.cdc.trustedintermediary.wrappers.MetricMetadata
import org.hl7.fhir.r4.model.MessageHeader
import spock.lang.Specification

class ConvertToOmlOrderTest extends Specification {

    def transformClass

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(MetricMetadata, Mock(MetricMetadata))
        TestApplicationContext.injectRegisteredImplementations()

        transformClass = new ConvertToOmlOrder()
    }

    def "convert ORM order to OML"() {
        given:
        def bundle = FhirBundleHelper.createMessageBundle(messageTypeCode: 'ORM_O01')

        expect:
        HapiHelper.resourceInBundle(bundle, MessageHeader).event.code == 'O01'

        when:
        transformClass.transform(new HapiFhirResource(bundle), null)
        def messageHeader = HapiHelper.resourceInBundle(bundle, MessageHeader) as MessageHeader

        then:
        messageHeader.getEvent().code == 'O21'
    }
}
