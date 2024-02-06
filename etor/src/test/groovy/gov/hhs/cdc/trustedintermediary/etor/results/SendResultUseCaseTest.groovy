package gov.hhs.cdc.trustedintermediary.etor.results

import gov.hhs.cdc.trustedintermediary.ResultMock
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.etor.messages.SendMessageUseCase
import gov.hhs.cdc.trustedintermediary.etor.messages.UnableToSendMessageException
import spock.lang.Specification

class SendResultUseCaseTest extends Specification {

    def mockSender = Mock(ResultSender)
    def mockConverter = Mock(ResultConverter)

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(SendMessageUseCase, SendResultUseCase.getInstance())
        TestApplicationContext.register(ResultConverter, mockConverter)
        TestApplicationContext.register(ResultSender, mockSender)
    }

    def "convertAndSend works"() {
        given:
        def mockResult = new ResultMock(null, "Mock result")
        TestApplicationContext.injectRegisteredImplementations()

        when:
        SendResultUseCase.getInstance().convertAndSend(mockResult)

        then:
        1 * mockConverter.addEtorProcessingTag(mockResult) >> mockResult
        1 * mockSender.send(mockResult) >> Optional.empty()
        noExceptionThrown()
    }

    def "convertAndSend throws exception when send fails"() {
        given:
        mockSender.send(_) >> { throw new UnableToSendMessageException("DogCow", new NullPointerException()) }
        TestApplicationContext.injectRegisteredImplementations()

        when:
        SendResultUseCase.getInstance().convertAndSend(Mock(Result))

        then:
        thrown(UnableToSendMessageException)
    }
}
