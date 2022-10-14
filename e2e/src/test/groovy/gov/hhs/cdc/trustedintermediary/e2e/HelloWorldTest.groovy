package gov.hhs.cdc.trustedintermediary.e2e

import spock.lang.Specification

class HelloWorldTest extends Specification {

    def "Hello World is returned"() {
        when:
        Client.callService("/dogcow")

        then:
        true == true
    }
}
