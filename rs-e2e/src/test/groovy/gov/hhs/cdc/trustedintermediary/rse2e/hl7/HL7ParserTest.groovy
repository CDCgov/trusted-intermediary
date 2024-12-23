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
}
