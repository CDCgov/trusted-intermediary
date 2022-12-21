package gov.hhs.cdc.trustedintermediary.etor.order

import gov.hhs.cdc.trustedintermediary.PojoTestUtils
import gov.hhs.cdc.trustedintermediary.context.ApplicationContext
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.wrappers.Logger
import gov.hhs.cdc.trustedintermediary.wrappers.Slf4jLogger
import spock.lang.Specification

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class OrderMessageTest extends Specification{
    def setup() {
        println('Setting up test data...')
        TestApplicationContext.reset()
        ApplicationContext.register(Logger.class, Slf4jLogger.getLogger())
    }

    def "test getters and setters"() {
        when:
        PojoTestUtils.validateGettersAndSetters(OrderMessage.class)

        then:
        noExceptionThrown()
    }

    def "test constructor"() {
        given:
        def id = "67890asdfg"
        def destination = "DogCow lab"
        def client = "fake hospital"
        def body = "lab order"
        def createAt = LocalDateTime.now(ZoneId.of("UTC"))
        def formattedTimeDate = createAt.format(DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm"))

        when:
        def orderMessage = new OrderMessage(id,destination,formattedTimeDate,client,body)

        then:
        orderMessage.getId() == id
        orderMessage.getDestination() == destination
        orderMessage.getClient() == client
        orderMessage.getBody() == body
        orderMessage.getCreatedAt() == formattedTimeDate
    }
}
