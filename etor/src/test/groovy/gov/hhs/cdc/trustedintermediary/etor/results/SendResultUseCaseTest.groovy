package gov.hhs.cdc.trustedintermediary.etor.results

import gov.hhs.cdc.trustedintermediary.ResultMock
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.etor.messages.MessageSender
import gov.hhs.cdc.trustedintermediary.etor.messages.SendMessageUseCase
import gov.hhs.cdc.trustedintermediary.etor.messages.UnableToSendMessageException
import spock.lang.Specification

class SendResultUseCaseTest extends Specification {

    def mockSender = Mock(MessageSender)

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(SendMessageUseCase, SendResultUseCase.getInstance())
        TestApplicationContext.register(MessageSender, mockSender)
    }

    def "convertAndSend works"() {
        given:
        def mockResult = new ResultMock(null, null, "Mock result")
        TestApplicationContext.injectRegisteredImplementations()

        when:
        SendResultUseCase.getInstance().convertAndSend(mockResult, _ as String)

        then:
        1 * mockSender.send(mockResult) >> Optional.empty()
        noExceptionThrown()
    }

    def "convertAndSend throws exception when send fails"() {
        given:
        mockSender.send(_) >> { throw new UnableToSendMessageException("DogCow", new NullPointerException()) }
        TestApplicationContext.injectRegisteredImplementations()

        when:
        SendResultUseCase.getInstance().convertAndSend(Mock(Result), _ as String)

        then:
        thrown(UnableToSendMessageException)
    }
}
