package gov.hhs.cdc.trustedintermediary.e2e

import spock.lang.Specification

class HelloWorldTest extends Specification {

    def "Hello World is returned"() {
        when:
        def response = Client.post("/v1/order")

        then:
        response == "DogCow requsted a lab order"
    }
}
