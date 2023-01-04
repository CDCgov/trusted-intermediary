package gov.hhs.cdc.trustedintermediary.etor.order

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainRequest
import gov.hhs.cdc.trustedintermediary.wrappers.Formatter
import gov.hhs.cdc.trustedintermediary.wrappers.FormatterProcessingException
import gov.hhs.cdc.trustedintermediary.external.jackson.JacksonFormatter
import spock.lang.Specification

class OrderControllerTest extends Specification {

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(OrderController, OrderController.getInstance())
    }

    def "parseOrder works"() {
        given:
        def mockOrderId = "asdf-12341-jkl-7890"

        def formatter = Mock(JacksonFormatter)
        formatter.convertToObject(_ as String, _ as Class) >> new Order(mockOrderId, "Massachusetts", "2022-12-21T08:34:27Z", "MassGeneral", "NBS panel for Clarus the DogCow")
        TestApplicationContext.register(Formatter, formatter)

        def request = new DomainRequest()

        TestApplicationContext.injectRegisteredImplementations()

        when:
        def parsedOrder = OrderController.getInstance().parseOrder(request)

        then:
        noExceptionThrown()
        parsedOrder.getId() == mockOrderId
    }

    def "parseOrder fails by the formatter"() {
        given:
        def formatter = Mock(JacksonFormatter)
        formatter.convertToObject(_ as String, _ as Class) >> { throw new FormatterProcessingException("unable to format or whatever", new Exception()) }
        TestApplicationContext.register(Formatter, formatter)

        def request = new DomainRequest()

        TestApplicationContext.injectRegisteredImplementations()

        when:
        OrderController.getInstance().parseOrder(request)

        then:
        thrown(RuntimeException)
    }

    def "constructResponse works"() {

        given:
        def mockBody = "DogCow goes Moof"

        def formatter = Mock(JacksonFormatter)
        formatter.convertToString(_ as OrderMessage) >> mockBody
        TestApplicationContext.register(Formatter, formatter)

        TestApplicationContext.injectRegisteredImplementations()

        when:
        def response = OrderController.getInstance().constructResponse(new OrderMessage("asdf-12341-jkl-7890", "Massachusetts", "2022-12-21T08:34:27Z", "MassGeneral", "NBS panel for Clarus the DogCow"))

        then:
        response.getBody() == mockBody
        response.getStatusCode() == 200
        response.getHeaders().get(OrderController.CONTENT_TYPE_LITERAL) == OrderController.APPLICATION_JSON_LITERAL
    }

    def "parseOrder fails by the formatter"() {
        given:
        def formatter = Mock(JacksonFormatter)
        formatter.convertToObject(_ as String, _ as Class) >> { throw new FormatterProcessingException("unable to format or whatever", new Exception()) }
        TestApplicationContext.register(Formatter, formatter)

        def request = new DomainRequest()

        TestApplicationContext.injectRegisteredImplementations()

        when:
        OrderController.getInstance().parseOrder(request)

        then:
        thrown(RuntimeException)
    }

    def "constructResponse fails to make the JSON"() {

        given:
        def formatter = Mock(JacksonFormatter)
        formatter.convertToString(_ as OrderMessage) >> { throw new FormatterProcessingException("couldn't make the JSON", new Exception()) }
        TestApplicationContext.register(Formatter, formatter)

        TestApplicationContext.injectRegisteredImplementations()

        when:
        OrderController.getInstance().constructResponse(new OrderMessage("asdf-12341-jkl-7890", "Massachusetts", "2022-12-21T08:34:27Z", "MassGeneral", "NBS panel for Clarus the DogCow"))

        then:
        thrown(RuntimeException)
    }
}
