package gov.hhs.cdc.trustedintermediary.rse2e.external.hapi

import ca.uhn.hl7v2.model.Message
import ca.uhn.hl7v2.model.v251.message.ORU_R01
import ca.uhn.hl7v2.model.v251.segment.MSH
import org.hl7.fhir.r4.model.Bundle
import spock.lang.Specification

class HapiHL7MessageTest extends Specification {

    def "getUnderlyingData should correctly initialize and return underlying data"() {
        given:
        def mockMessage = Mock(Message)
        def hl7Message = new HapiHL7Message(mockMessage)

        expect:
        hl7Message.getUnderlyingData() == mockMessage
    }

    def "getIdentifier should return the MSH-10 identifier of the underlying message"() {
        given:
        def expectedIdentifier = "0001"
        def oruMessage = new ORU_R01()
        MSH mshSegment = oruMessage.getMSH()
        mshSegment.getMessageControlID().setValue(expectedIdentifier)

        and:
        def hl7Message = new HapiHL7Message(oruMessage)

        when:
        def name = hl7Message.getIdentifier()

        then:
        name == expectedIdentifier
    }
}
