package gov.hhs.cdc.trustedintermediary.e2e

import spock.lang.Specification

class HappyLabOrderRequestTest extends Specification{

    def "happy order test"() {
        given:
        var destination = "DogCow lab"
        var client = "Mock Hospital"
        var body = "lab order"

        when:
        def response = Client.post(
                "/v1/etor/order",
                Map.of("Destination", "DogCow lab", "Client", "Mock Hospital"),
                "lab order"
                )

        then:
        response == """{
  "id" : "missing id",
  "destination" : "$destination",
  "createdAt" : "missing timestamp",
  "client" : "$client",
  "body" : "$body"
}"""
    }
}
