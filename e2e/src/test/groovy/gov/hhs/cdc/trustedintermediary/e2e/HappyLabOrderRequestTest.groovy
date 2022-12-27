package gov.hhs.cdc.trustedintermediary.e2e

import spock.lang.Specification

class HappyLabOrderRequestTest extends Specification{

    def "happy order test"() {
        given:
        var destination = "DogCow lab"
        var client = "Mock Hospital"
        var content = "MSH|lab order"

        when:

        def response = Client.post(
                "/v1/etor/order",
                Map.of("Content-Type", "text/plain"),
                "{\"client\":\"$client\", \"destination\":\"$destination\", \"content\":\"$content\"}"
                )
        def expected =
                """\"{\\"id\\":\\"missing id\\",\\"destination\\":\\"$destination\\",\\"createdAt\\":\\"missing timestamp\\",\\"client\\":\\"$client\\",\\"content\\":\\"$content\\"}\""""
        then:
        response == expected
    }
}
