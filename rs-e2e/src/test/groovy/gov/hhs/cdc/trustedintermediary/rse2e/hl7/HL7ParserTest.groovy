package gov.hhs.cdc.trustedintermediary.rse2e.hl7


import spock.lang.Specification

class HL7ParserTest extends Specification {

    def "parseMessage should handle basic HL7 message"() {
        given:
        def content = """MSH|^~\\&|sending_app|sending_facility
PID|||12345||Doe^John||19800101|M"""

        when:
        def message = HL7Parser.parseMessage(content)
        def segments = message.getSegments()

        then:
        segments.size() == 2
        segments[0].name() == "MSH"
        segments[0].fields().size() == 4
        segments[0].fields().get(0) == "|"
        segments[0].fields().get(1) == "^~\\&"
        segments[1].name() == "PID"
        segments[1].fields().size() == 8
        segments[1].fields().get(2) == "12345"
        segments[1].fields().get(4) == "Doe^John"
    }

    def "parseMessage should handle empty lines in message"() {
        given:
        def content = """MSH|^~\\&|sending_app

PID|||12345"""

        when:
        def result = HL7Parser.parseMessage(content)

        then:
        result.getSegments().size() == 2
    }

    def "parseMessage should preserve empty fields"() {
        given:
        def content = "MSH|^~\\&|sending_app||sending_facility"

        when:
        def result = HL7Parser.parseMessage(content)

        then:
        result.getSegments().get(0).fields().get(3) == ""
    }

    def "parseMessageFieldValue should handle different field levels"() {
        given:
        def message = HL7Parser.parseMessage("TST|value1|component1^component2|rep1~rep2^|comp1^~sub1&sub2")

        when:
        def hl7Path = HL7Parser.parsePath(path)
        def result = HL7Parser.parseMessageFieldValue(message, hl7Path)

        then:
        result == expectedValue

        where:
        scenario           | path          | expectedValue
        "simple field"     | "TST-1"       | "value1"
        "component"        | "TST-2.2"     | "component2"
        "repetition"       | "TST-3.1.2"   | "rep2"
        "subcomponent"     | "TST-4.2.2.2" | "sub2"
        "invalid index"    | "TST-5"       | ""
    }

    def "parseMessageFieldValue returns an empty string when inputs are null"() {
        when:
        def result = HL7Parser.parseMessageFieldValue(null, null)

        then:
        result == ""
    }

    def "parseMessageFieldValue returns an empty string if an empty message is given"() {
        given:
        def hl7Path = HL7Parser.parsePath("MSH-3")
        def message = HL7Parser.parseMessage("")

        when:
        def out = HL7Parser.parseMessageFieldValue(message, hl7Path)

        then:
        out == ""
    }

    def "parseMessageFieldValue returns an empty string if an empty hl7 path is given"() {
        given:
        def hl7Path = HL7Parser.parsePath("")

        when:
        def out = HL7Parser.parseMessageFieldValue(_ as HL7Message, hl7Path)

        then:
        out == ""
    }

    def "parseMessageFieldValue returns an empty string if the indices in hl7 path are pointing outside the expected range"() {
        given:
        def message = HL7Parser.parseMessage("MSH|fakeValues\nOBR|fakeValues")
        def hl7Path = HL7Parser.parsePath("MSH-3")

        when:
        def out = HL7Parser.parseMessageFieldValue(message, hl7Path)

        then:
        out == ""
    }
}
