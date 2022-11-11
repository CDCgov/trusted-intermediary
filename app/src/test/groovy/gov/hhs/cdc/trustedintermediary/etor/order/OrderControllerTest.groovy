package gov.hhs.cdc.trustedintermediary.etor.order

import spock.lang.Specification

class OrderControllerTest extends Specification {
    def "parseOrder works"() {
        when:
        def parsedOrder = OrderController.getInstance().parseOrder("DogCow")

        then:
        parsedOrder == "DogCow sent in a lab order"
    }
}
