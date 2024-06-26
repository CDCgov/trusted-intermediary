package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom


import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiFhirHelper
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiFhirResource
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiHelper
import gov.hhs.cdc.trustedintermediary.wrappers.MetricMetadata
import spock.lang.Specification

class AddEtorProcessingTagTest extends Specification {

    def transformClass

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(MetricMetadata, Mock(MetricMetadata))
        TestApplicationContext.injectRegisteredImplementations()

        transformClass = new AddEtorProcessingTag()
    }

    def "add ETOR meta tag to message header"() {
        given:
        def bundle = HapiFhirHelper.createMessageBundle(messageTypeCode: 'ORM_O01')

        expect:
        HapiHelper.getMSHMessageHeader(bundle).getMeta().getTag().size() == 0

        when:
        transformClass.transform(new HapiFhirResource(bundle), null)
        def messageHeader = HapiHelper.getMSHMessageHeader(bundle)

        then:
        messageHeader.getMeta().getTag().last().code == "ETOR"
    }
}
