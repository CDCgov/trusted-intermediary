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
        def messageHeader = HapiHelper.getMessageHeader(bundle)
        def displayArray = messageHeader.getEventCoding().getDisplay().split("\\^")

        expect:
        displayArray.size() == 3
        displayArray[2] != ""

        when:
        transformClass.transform(new HapiFhirResource(bundle), null)
        def convertedMessageHeader = HapiHelper.getMessageHeader(bundle)
        def convertedDisplay = convertedMessageHeader.getEventCoding().getDisplay()
        def convertedDisplayArray = convertedDisplay.split("\\^")

        then:
        convertedDisplayArray.size() == 2
    }

    def "don't do anything if message type structure not present"() {
        given:
        def messageTypeDisplay = "ORU^R01"
        def bundle = new Bundle()
        def messageHeader = HapiHelper.getOrCreateMessageHeader(bundle)
        messageHeader.setEvent(new Coding().setDisplay(messageTypeDisplay))

        when:
        transformClass.transform(new HapiFhirResource(bundle), null)
        def convertedMessageHeader = HapiHelper.getMessageHeader(bundle)
        def convertedDisplay = convertedMessageHeader.getEventCoding().getDisplay()

        then:
        convertedDisplay == messageTypeDisplay
    }
}
