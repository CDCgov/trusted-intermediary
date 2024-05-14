package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom

import gov.hhs.cdc.trustedintermediary.FhirBundleHelper
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiFhirResource
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiHelper
import spock.lang.Specification

class RemoveMessageTypeStructureTest  extends Specification {

    def transformClass

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.injectRegisteredImplementations()

        transformClass = new removeMessageTypeStructure()
    }

    def "remove message type structure"() {
        given:
        def bundle = FhirBundleHelper.createMessageBundle(messageTypeCode: 'ORM_O01')
        def messageHeader = HapiHelper.getOrCreateMessageHeader(bundle)
        def displayArray = messageHeader.getEventCoding().getDisplay().split("\\^")

        expect:
        displayArray.size() == 3
        displayArray[2] != ""

        when:
        transformClass.transform(new HapiFhirResource(bundle), null)
        def convertedMessageHeader = HapiHelper.getOrCreateMessageHeader(bundle)
        def convertedDisplay = convertedMessageHeader.getEventCoding().getDisplay()
        def convertedDisplayArray = convertedDisplay.split("\\^")

        then:
        convertedDisplayArray.size() == 2
    }
}
