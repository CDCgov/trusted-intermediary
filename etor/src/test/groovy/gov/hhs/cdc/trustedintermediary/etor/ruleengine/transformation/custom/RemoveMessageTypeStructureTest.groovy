package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom

import gov.hhs.cdc.trustedintermediary.FhirBundleHelper
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiFhirResource
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiHelper
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Coding
import spock.lang.Specification

class RemoveMessageTypeStructureTest  extends Specification {

    def transformClass

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.injectRegisteredImplementations()

        transformClass = new RemoveMessageTypeStructure()
    }

    def "remove message type structure"() {
        given:
        def bundle = FhirBundleHelper.createMessageBundle(messageTypeCode: 'ORM_O01')
        def msh9_3 = HapiHelper.getMSH9_3Value(bundle)
        def msh9_3Array = msh9_3.split("\\^")

        expect:
        msh9_3Array.size() == 3
        msh9_3Array[2] != ""

        when:
        transformClass.transform(new HapiFhirResource(bundle), null)
        def convertedMsh9_3 = HapiHelper.getMSH9_3Value(bundle)
        def convertedMsh9_3Array = convertedMsh9_3.split("\\^")

        then:
        convertedMsh9_3Array.size() == 2
    }

    def "don't do anything if message type structure not present"() {
        given:
        def messageTypeDisplay = "ORU^R01"
        def bundle = new Bundle()
        def messageHeader = FhirBundleHelper.createMSHMessageHeader(bundle)
        messageHeader.setEvent(new Coding().setDisplay(messageTypeDisplay))

        when:
        transformClass.transform(new HapiFhirResource(bundle), null)
        def convertedMessageHeader = HapiHelper.getMSHMessageHeader(bundle)
        def convertedDisplay = convertedMessageHeader.getEventCoding().getDisplay()

        then:
        convertedDisplay == messageTypeDisplay
    }

    def "don't throw exception if message type coding not present"() {
        given:
        def bundle = new Bundle()
        FhirBundleHelper.createMSHMessageHeader(bundle)

        when:
        transformClass.transform(new HapiFhirResource(bundle), null)

        then:
        noExceptionThrown()
    }
}
