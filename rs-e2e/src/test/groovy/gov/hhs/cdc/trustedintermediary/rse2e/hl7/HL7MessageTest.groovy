package gov.hhs.cdc.trustedintermediary.rse2e.hl7

import spock.lang.Specification

class HL7MessageTest extends Specification {
    def "getUnderlyingData should correctly return itself"() {
        given:
        def messageContent = """MSH|^~\\&|Epic^1.2.840.114350.1.13.145.2.7.2.695071^ISO
PID|1||11102779^^^CR^MR||SMITH^BB SARAH^^^^^L"""
        def hl7Message = HL7Parser.parse(messageContent)

        expect:
        hl7Message.getUnderlyingData() == hl7Message
    }

    def "getIdentifier should return the MSH-10 identifier of the underlying message"() {
        given:
        def expectedIdentifier = "001"
        def messageContent = "MSH|^~\\&|SISGDSP|SISGDSP|SISHIERECEIVER^11223344^L,M,N|^^L,M,N|20230706093730||ORU^R01^ORU_R01|" + expectedIdentifier + "|D|2.5.1"
        def hl7Message = HL7Parser.parse(messageContent)

        when:
        def name = hl7Message.getIdentifier()

        then:
        name == expectedIdentifier
    }
}
