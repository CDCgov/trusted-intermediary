package gov.hhs.cdc.trustedintermediary.e2e

import spock.lang.Specification

class HelloWorldTest extends Specification {

    def "DogCow is returned from demo ETOR order endpoint"() {
        when:
        def response = Client.post("/v1/etor/order")

        then:
        response == "DogCow sent in a lab order"
    }
}
