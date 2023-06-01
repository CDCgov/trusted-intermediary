package gov.hhs.cdc.trustedintermediary.auth

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import spock.lang.Specification

class AuthControllerTest extends Specification {
    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.injectRegisteredImplementations()
    }

    def "extractFormUrlEncode grabs everything"() {
    }

    def "extractFormUrlEncode gives an empty value with key="() {
    }

    def "extractFormUrlEncode gives an empty value with key (and no equals)"() {
    }

    def "extractFormUrlEncode gives an empty key with a beginning &"() {
    }
}
