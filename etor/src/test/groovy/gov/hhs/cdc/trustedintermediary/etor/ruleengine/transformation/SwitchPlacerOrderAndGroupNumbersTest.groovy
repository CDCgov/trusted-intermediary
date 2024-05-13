package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation


import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom.switchPlacerOrderAndGroupNumbers
import spock.lang.Specification

class SwitchPlacerOrderAndGroupNumbersTest extends Specification {
    def transformClass

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.injectRegisteredImplementations()

        transformClass = new switchPlacerOrderAndGroupNumbers()
    }
}
