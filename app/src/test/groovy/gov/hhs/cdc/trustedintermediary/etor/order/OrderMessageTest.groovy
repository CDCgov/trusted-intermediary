package gov.hhs.cdc.trustedintermediary.etor.order

import gov.hhs.cdc.trustedintermediary.PojoTestUtils
import spock.lang.Specification

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class OrderMessageTest extends Specification{

    def "test getters and setters"() {
        when:
        PojoTestUtils.validateGettersAndSetters(OrderMessage.class)

        then:
        noExceptionThrown()
    }

    def "test order constructor"() {
        given:
        def id = "67890asdfg"
        def destination = "DogCow lab"
        def client = "fake hospital"
        def body = "lab order"
        def createAt = LocalDateTime.now(ZoneId.of("UTC"))
        def formattedTimeDate = createAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

        def order = new Order(id, destination, formattedTimeDate, client, body)

        when:
        def orderMessage = new OrderMessage(order)

        then:
        orderMessage.getId() == id
        orderMessage.getDestination() == destination
        orderMessage.getClient() == client
        orderMessage.getContent() == body
        orderMessage.getCreatedAt() == formattedTimeDate
    }

    def "test argument constructor"() {
        given:
        def id = "67890asdfg"
        def destination = "DogCow lab"
        def client = "fake hospital"
        def body = "lab order"
        def createAt = LocalDateTime.now(ZoneId.of("UTC"))
        def formattedTimeDate = createAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

        when:
        def orderMessage = new OrderMessage(id, destination, formattedTimeDate, client, body)

        then:
        orderMessage.getId() == id
        orderMessage.getDestination() == destination
        orderMessage.getClient() == client
        orderMessage.getContent() == body
        orderMessage.getCreatedAt() == formattedTimeDate
    }
}
