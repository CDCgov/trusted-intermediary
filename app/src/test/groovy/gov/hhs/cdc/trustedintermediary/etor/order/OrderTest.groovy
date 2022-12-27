package gov.hhs.cdc.trustedintermediary.etor.order

import gov.hhs.cdc.trustedintermediary.PojoTestUtils
import gov.hhs.cdc.trustedintermediary.context.ApplicationContext
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.wrappers.Formatter
import gov.hhs.cdc.trustedintermediary.wrappers.JacksonFormatter
import gov.hhs.cdc.trustedintermediary.wrappers.Logger
import gov.hhs.cdc.trustedintermediary.wrappers.Slf4jLogger
import spock.lang.Specification

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class OrderTest extends Specification{

    def setup() {
        println('Setting up test data...')
        TestApplicationContext.reset()
        ApplicationContext.register(Logger.class, Slf4jLogger.getLogger())
        ApplicationContext.register(Formatter.class, new JacksonFormatter())
    }

    def "test getters and setters"() {
        when:
        PojoTestUtils.validateGettersAndSetters(Order.class)

        then:
        noExceptionThrown()
    }

    def "test constructor"() {
        given:
        def id = "12345werty"
        def destination = "fake lab"
        def client = "fake hospital"
        def body = "lab order"
        def createAt = LocalDateTime.now(ZoneId.of("UTC"))
        def formattedTimeDate = createAt.format(DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm"))

        when:
        def order = new Order(id,destination,createAt.toString(),client,body)

        then:
        order.getId() == id
        order.getDestination() == destination
        order.getClient() == client
        order.getContent() == body
        order.getCreatedAt() == formattedTimeDate
    }
}
