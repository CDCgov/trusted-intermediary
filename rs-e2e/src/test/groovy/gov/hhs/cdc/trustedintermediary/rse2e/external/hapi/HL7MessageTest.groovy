package gov.hhs.cdc.trustedintermediary.rse2e.external.hapi

import gov.hhs.cdc.trustedintermediary.rse2e.HL7Message
import spock.lang.Specification

class HL7MessageTest extends Specification {

    def "getUnderlyingData should correctly return itself"() {
        given:
        def segments = [
            MSH: [
                "MSH|^~\\&|Epic^1.2.840.114350.1.13.145.2.7.2.695071^ISO\n"
            ],
            PID: [
                "PID|1||11102779^^^CR^MR||SMITH^BB SARAH^^^^^L\n"
            ]
        ]
        def hl7Message = new HL7Message(segments as Map<String, List<String>>, "|^~\\&" as char[])

        expect:
        hl7Message.getUnderlyingData() == hl7Message
    }

    def "getIdentifier should return the MSH-10 identifier of the underlying message"() {
        given:
        def expectedIdentifier = "0001"
        def segments = [
            MSH: [
                "MSH|^~\\&|Epic^1.2.840.114350.1.13.145.2.7.2.695071^ISO\n"
            ],
            PID: [
                "PID|1||11102779^^^CR^MR||SMITH^BB SARAH^^^^^L\n"
            ]
        ]

        and:
        def hl7Message = new HL7Message(segments, "|^~\\&" as char[])

        when:
        def name = hl7Message.getIdentifier()

        then:
        name == expectedIdentifier
    }
}
