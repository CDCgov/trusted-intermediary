package gov.hhs.cdc.trustedintermediary.rse2e.hl7


import spock.lang.Specification

class HL7ParserTest extends Specification {

    def "getValue returns the expected value given the segment name and indices"() {
        given:
        def content = """MSH|^~\\&|Sender Application^sender.test.com^DNS|Sender Facility^0.0.0.0.0.0.0.0^ISO|Receiver Application^0.0.0.0.0.0.0.0^ISO|Receiver Facility^automated-staging-test-receiver-id^DNS|20230101010000-0000||ORM^O01^ORM_O01|001|N|2.5.1||||||||||
PID|1||1300974^^^Baptist East^MR||ONE^TESTCASE||202402210152-0500|F^Female^HL70001||2106-3^White^HL70005|1234 GPCS WAY^^MONTGOMERY^Alabama^36117^USA^home^^Montgomery|||||||2227600015||||N^Not Hispanic or Latino^HL70189|||1|||||||||||||||LRI_NG_FRN_PROFILE^^2.16.840.1.113883.9.195.3.4^ISO~LRI_NDBS_COMPONENT^^2.16.840.1.113883.9.195.3.6^ISO~LAB_PRN_Component^^2.16.840.1.113883.9.81^ISO~LAB_TO_COMPONENT^^2.16.840.1.113883.9.22^ISO|
NK1|1|ONE^MOMFIRST|MTH^Mother^HL70063||^^^^^804^5693861||||||||||||||||||||||||||||123456789^^^Medicaid&2.16.840.1.113883.4.446&ISO^MD||||000-00-0000^^^ssn&2.16.840.1.113883.4.1&ISO^SS
ORC|NW|4560411583^ORDERID||||||||||12345^^^^^^^^NPI&2.16.840.1.113883.4.6&ISO^L|||||||||
OBR|1|4560411583^ORDERID||54089-8^Newborn screening panel AHIC^LN|||202402221854-0500|||||||||12345^^^^^^^^NPI&2.16.840.1.113883.4.6&ISO^L||||||||
OBX|1|ST|57723-9^Unique bar code number of Current sample^LN||123456||||||F|||202402221854-0500
"""

        when:
        HL7Message message = HL7Parser.parse(content)

        then:
        message.getValue("PID", 3) == "1300974^^^Baptist East^MR"
        message.getValue("PID", 3, 0) == null
        message.getValue("PID", 3, 1) == "1300974"
        message.getValue("PID", 3, 2) == ""
        message.getValue("PID", 3, 4) == "Baptist East"
        message.getValue("PID", 3, 5) == "MR"
        message.getValue("PID", 3, 6) == null
        message.getValue("PID", 40, 4, 1) == "ISO"
        message.getValue("PID", 40, 4, 2) == "LRI_NDBS_COMPONENT"
        message.getValue("PID", 40, 4, 3) == null
        message.getValue("NK1", 33, 4, 1, 1) == "Medicaid"
        message.getValue("NK1", 33, 4, 1, 1, 1) == null
    }

    def "parseAndGetValue returns null if a null list of fields is given"() {
        given:
        def nullList = null
        def delimiters = ['|']

        when:
        def out = HL7Parser.parseAndGetValue(nullList, delimiters as char[])

        then:
        out == null
    }

    def "parseAndGetValue returns null if an empty list of fields is given"() {
        given:
        def emptyList = []
        def delimiters = ['|']

        when:
        def out = HL7Parser.parseAndGetValue(emptyList, delimiters as char[])

        then:
        out == null
    }

    def "parseAndGetValue returns null if the indices are pointing outside the expected range"() {
        given:
        def emptyList = [
            "MSH|fakeValues",
            "OBR|fakeValues"
        ]
        def delimiters = ['|']

        when:
        def out = HL7Parser.parseAndGetValue(emptyList, delimiters as char[], 10, 20)

        then:
        out == null
    }
}
