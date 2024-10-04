package gov.hhs.cdc.trustedintermediary.rse2e.external.hapi

import ca.uhn.hl7v2.model.Message
import spock.lang.Specification

class HapiHL7MessageTest extends Specification {

    def "should correctly initialize and return underlying data"() {
        given:
        def mockMessage = Mock(Message)
        def hl7Message = new HapiHL7Message(mockMessage)

        expect:
        hl7Message.getUnderlyingData() == mockMessage
    }

    def "should return the name of the underlying message"() {
        given:
        def expectedName = "TestMessage"
        def message = Mock(Message)
        message.getName() >> expectedName

        and:
        def hl7Message = new HapiHL7Message(message)

        when:
        def name = hl7Message.getName()

        then:
        name == expectedName
    }
}
