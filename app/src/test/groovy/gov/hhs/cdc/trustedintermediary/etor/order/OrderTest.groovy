package gov.hhs.cdc.trustedintermediary.etor.order

import gov.hhs.cdc.trustedintermediary.PojoTestUtils
import spock.lang.Specification

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class OrderTest extends Specification {

    def "test getters and setters"() {
        when:
        PojoTestUtils.validateGettersAndSetters(Order.class)

        then:
        noExceptionThrown()
    }

    def "test default constructor"() {
        when:
        def order = new Order()

        then:
        order.getId() == null
        order.getDestination() == null
        order.getClient() == null
        order.getContent() == null
        order.getCreatedAt() == null
    }

    def "test argument constructor"() {
        given:
        def id = "12345werty"
        def destination = "fake lab"
        def client = "fake hospital"
        def body = "lab order"
        def createdAt = LocalDateTime.now(ZoneId.of("UTC"))
        def formattedTimeDate = createdAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

        when:
        def order = new Order(id, destination, formattedTimeDate, client, body)

        then:
        order.getId() == id
        order.getDestination() == destination
        order.getClient() == client
        order.getContent() == body
        order.getCreatedAt() == formattedTimeDate
    }
}
