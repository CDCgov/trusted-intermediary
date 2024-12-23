package gov.hhs.cdc.trustedintermediary.rse2e.hl7

import spock.lang.Specification

class HL7MessageTest extends Specification {

    HL7Message message
    String messageContent

    def setup() {
        messageContent = """MSH|^~\\&|Sender Application^sender.test.com^DNS|Sender Facility^0.0.0.0.0.0.0.0^ISO|Receiver Application^0.0.0.0.0.0.0.0^ISO|Receiver Facility^automated-staging-test-receiver-id^DNS|20230101010000-0000||ORM^O01^ORM_O01|001|N|2.5.1||||||||||
PID|1||1300974^^^Baptist East^MR||ONE^TESTCASE||202402210152-0500|F^Female^HL70001||2106-3^White^HL70005|1234 GPCS WAY^^MONTGOMERY^Alabama^36117^USA^home^^Montgomery|||||||2227600015||||N^Not Hispanic or Latino^HL70189|||1|||||||||||||||LRI_NG_FRN_PROFILE^^2.16.840.1.113883.9.195.3.4^ISO~LRI_NDBS_COMPONENT^^2.16.840.1.113883.9.195.3.6^ISO~LAB_PRN_Component^^2.16.840.1.113883.9.81^ISO~LAB_TO_COMPONENT^^2.16.840.1.113883.9.22^ISO|
NK1|1|ONE^MOMFIRST|MTH^Mother^HL70063||^^^^^804^5693861||||||||||||||||||||||||||||123456789^^^Medicaid&2.16.840.1.113883.4.446&ISO^MD||||000-00-0000^^^ssn&2.16.840.1.113883.4.1&ISO^SS
ORC|NW|4560411583^ORDERID||||||||||12345^^^^^^^^NPI&2.16.840.1.113883.4.6&ISO^L|||||||||
OBR|1|4560411583^ORDERID||54089-8^Newborn screening panel AHIC^LN|||202402221854-0500|||||||||12345^^^^^^^^NPI&2.16.840.1.113883.4.6&ISO^L||||||||
OBX|1|ST|57723-9^Unique bar code number of Current sample^LN||123456||||||F|||202402221854-0500
OBX|2|NM|||3122||||||F|||202402221854-0500||"""
        message = HL7Parser.parseMessage(messageContent)
    }

    def "getUnderlyingData should correctly return itself"() {
        expect:
        message.getUnderlyingData() == message
    }

    def "toString should return the original string content"() {
        expect:
        message.toString() == messageContent
    }

    def "getIdentifier should return the MSH-10 identifier of the underlying message"() {
        expect:
        message.getIdentifier() == "001"
    }

    def "getSegments returns the expected segments"() {
        when:
        def actualSegment = message.getSegments("OBX").get(1)

        then:
        actualSegment.name() == "OBX"
        actualSegment.fields().get(0) == "2"
    }

    def "getSegments returns all segments if no called with no arguments"() {
        expect:
        message.getSegments().size() == 7
    }

    def "getSegmentCount returns the expected number of segments"() {
        expect:
        message.getSegmentCount("MSH") == 1
        message.getSegmentCount("PID") == 1
        message.getSegmentCount("OBX") == 2
    }

    def "hasSegment returns expected boolean"() {
        expect:
        message.hasSegment("MSH", 0)
        message.hasSegment("PID", 0)
        message.hasSegment("OBX", 0)
        message.hasSegment("OBX", 1)
        !message.hasSegment("MSH", 1)
        !message.hasSegment("ZZZ", 0)
    }

    def "getSegment returns the expected segment"() {
        when:
        def obxSegment1 = message.getSegment("OBX")
        def obxSegment2 = message.getSegment("OBX", 1)

        then:
        obxSegment1.name() == "OBX"
        obxSegment1.fields().get(0) == "1"
        obxSegment2.name() == "OBX"
        obxSegment2.fields().get(0) == "2"
    }

    def "getValue returns the expected value given the segment name and indices"() {
        expect:
        message.getValue("PID-3") == "1300974^^^Baptist East^MR"
        message.getValue("PID-3.0") == ""
        message.getValue("PID-3.1") == "1300974"
        message.getValue("PID-3.2") == ""
        message.getValue("PID-3.4") == "Baptist East"
        message.getValue("PID-3.5") == "MR"
        message.getValue("PID-3.6") == ""
        message.getValue("PID-40.4.1") == "ISO"
        message.getValue("PID-40.4.2") == "LRI_NDBS_COMPONENT"
        message.getValue("PID-40.4.3") == ""
        message.getValue("NK1-33.4.1.1") == "Medicaid"
        message.getValue("PID-99") == ""
    }

    def "getValue should throws an exception when the hl7 field index has too many sublevels"() {
        when:
        message.getValue("NK1-33.4.1.1.1")

        then:
        thrown(HL7ParserException)
    }

    def "getValue should throws an exception when the segment is not found"() {
        when:
        message.getValue("ZZZ-1")

        then:
        thrown(HL7MessageException)
    }
}
