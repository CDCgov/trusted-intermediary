package gov.hhs.cdc.trustedintermediary.external.reportstream

import gov.hhs.cdc.trustedintermediary.ResultMock
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.etor.messages.MessageSender
import spock.lang.Specification

class ReportStreamResultSenderTest extends Specification {

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(MessageSender, ReportStreamResultSender.getInstance())
    }

    def "send results works"() {
        when:
        ReportStreamResultSender.getInstance().send(new ResultMock(null, null, "Mock result"))

        then:
        noExceptionThrown()
    }
}
