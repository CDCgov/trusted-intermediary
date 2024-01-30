package gov.hhs.cdc.trustedintermediary.external.reportstream

import gov.hhs.cdc.trustedintermediary.ResultMock
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.etor.results.ResultSender
import spock.lang.Specification

class ReportStreamResultSenderTest extends Specification {

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(ResultSender, ReportStreamResultSender.getInstance())
    }

    def "send results works"() {
        when:
        ReportStreamResultSender.getInstance().send(new ResultMock(null, "Mock result"))

        then:
        noExceptionThrown()
    }
}
