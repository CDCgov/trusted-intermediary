package gov.hhs.cdc.trustedintermediary.e2e

import spock.lang.Specification

class EmptyLabOrderRequestTest extends Specification {

    def "order message is returned from demo ETOR order endpoint"() {
        when:
        def response = Client.post("/v1/etor/order")

        then:
        response ==
                """{
  "id" : "missing id",
  "destination" : "missing destination",
  "createdAt" : "missing timestamp",
  "client" : "missing client",
  "body" : "missing body"
}"""
    }
}
