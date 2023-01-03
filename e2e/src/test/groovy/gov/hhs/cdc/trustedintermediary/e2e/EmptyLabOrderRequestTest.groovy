package gov.hhs.cdc.trustedintermediary.e2e

import spock.lang.Specification

class EmptyLabOrderRequestTest extends Specification {

    def "order message is returned from demo ETOR order endpoint"() {
        when:
        def response = Client.post("/v1/etor/order", Map.of("Content-Type", "application/json"),"""{"client":"missing client"}""")
        def expected =
                """\"{\\"id\\":\\"missing id\\",\\"destination\\":\\"missing destination\\",\\"createdAt\\":\\"missing timestamp\\",\\"client\\":\\"missing client\\",\\"content\\":\\"missing content\\"}\""""

        then:
        response == expected
    }
}
