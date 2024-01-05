package gov.hhs.cdc.trustedintermediary.etor.orders

import gov.hhs.cdc.trustedintermediary.OrderMock
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.etor.metadata.EtorMetadataStep
import gov.hhs.cdc.trustedintermediary.etor.metadata.PartnerMetadataOrchestrator
import gov.hhs.cdc.trustedintermediary.utils.SyncRetryTask
import gov.hhs.cdc.trustedintermediary.wrappers.MetricMetadata
import spock.lang.Specification

import java.util.concurrent.Callable

class SendOrderUsecaseTest extends Specification {

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(SendOrderUseCase, SendOrderUseCase.getInstance())
        TestApplicationContext.register(MetricMetadata, Mock(MetricMetadata))
        TestApplicationContext.register(PartnerMetadataOrchestrator, Mock(PartnerMetadataOrchestrator))
    }

    def "send sends successfully"() {
        given:
        def receivedSubmissionId = "receivedId"
        def sentSubmissionId = "sentId"

        def sendOrder = SendOrderUseCase.getInstance()
        def mockOrder = new OrderMock(null, null, null)
        def mockOmlOrder = Mock(Order)

        def mockOrchestrator = Mock(PartnerMetadataOrchestrator)
        TestApplicationContext.register(PartnerMetadataOrchestrator, mockOrchestrator)

        def mockRetryTask = Mock(SyncRetryTask)
        mockRetryTask.retry({ it.call(); true }, _, _) >> { Callable<Void> task, int retries, int delay -> task.call(); true }
        TestApplicationContext.register(SyncRetryTask, mockRetryTask)

        def mockConverter = Mock(OrderConverter)
        TestApplicationContext.register(OrderConverter, mockConverter)

        def mockSender = Mock(OrderSender)
        TestApplicationContext.register(OrderSender, mockSender)

        TestApplicationContext.injectRegisteredImplementations()

        when:
        sendOrder.convertAndSend(mockOrder, receivedSubmissionId)

        then:
        1 * mockConverter.convertMetadataToOmlOrder(mockOrder) >> mockOmlOrder
        1 * mockConverter.addContactSectionToPatientResource(mockOmlOrder) >> mockOmlOrder
        1 * mockSender.sendOrder(mockOmlOrder) >> Optional.of(sentSubmissionId)
        1 * sendOrder.metadata.put(_, EtorMetadataStep.ORDER_CONVERTED_TO_OML)
        1 * sendOrder.metadata.put(_, EtorMetadataStep.CONTACT_SECTION_ADDED_TO_PATIENT)
        1 * mockOrchestrator.updateMetadataForReceivedOrder(receivedSubmissionId, _ as String)
        1 * mockOrchestrator.updateMetadataForSentOrder(receivedSubmissionId, sentSubmissionId)
    }

    def "send fails to send"() {
        given:
        def mockOrder = new OrderMock(null, null, null)
        def mockConverter = Mock(OrderConverter)
        def mockSender = Mock(OrderSender)
        mockSender.sendOrder(_) >> { throw new UnableToSendOrderException("DogCow", new NullPointerException()) }

        TestApplicationContext.register(OrderConverter, mockConverter)
        TestApplicationContext.register(OrderSender, mockSender)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        SendOrderUseCase.getInstance().convertAndSend(mockOrder, _ as String)

        then:
        thrown(UnableToSendOrderException)
    }
}
