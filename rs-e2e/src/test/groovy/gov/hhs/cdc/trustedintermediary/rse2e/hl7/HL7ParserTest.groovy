package gov.hhs.cdc.trustedintermediary.rse2e.hl7


import spock.lang.Specification

class HL7ParserTest extends Specification {

    def "parse should handle basic HL7 message"() {
        given:
        def content = """MSH|^~\\&|sending_app|sending_facility
PID|||12345||Doe^John||19800101|M"""

        when:
        def message = HL7Parser.parse(content)
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

    def "parse should handle empty lines in message"() {
        given:
        def content = """MSH|^~\\&|sending_app

PID|||12345"""

        when:
        def result = HL7Parser.parse(content)

        then:
        result.getSegments().size() == 2
    }

    def "parse should preserve empty fields"() {
        given:
        def content = "MSH|^~\\&|sending_app||sending_facility"

        when:
        def result = HL7Parser.parse(content)

        then:
        result.getSegments().get(0).fields().get(3) == ""
    }

    def "parseAndGetValue should handle different field levels"() {
        given:
        def fields = [
            "value1",
            "component1^component2",
            "rep1~rep2^",
            "comp1^~sub1&sub2"
        ]
        def delimiters = ['|', '^', '~', '&'] as char[]

        when:
        def result = HL7Parser.parseAndGetValue(fields, delimiters, indices as int[])

        then:
        result == expectedValue

        where:
        scenario           | indices      | expectedValue
        "simple field"     | [1]          | "value1"
        "component"        | [2, 2]       | "component2"
        "repetition"       | [3, 1, 2]    | "rep2"
        "subcomponent"     | [4, 2, 2, 2] | "sub2"
        "invalid index"    | [5]          | ""
    }

    def "parseAndGetValue returns an empty string when inputs are null"() {
        when:
        def result = HL7Parser.parseAndGetValue(null, [] as char[], 1)

        then:
        result == ""
    }

    def "getEncodingCharacterMap should use defaults when no encoding characters provided"() {
        when:
        def encodingChars = HL7Parser.getEncodingCharacterMap(null)

        then:
        encodingChars["field"] == '|' as char
        encodingChars["component"] == '^' as char
        encodingChars["repetition"] == '~' as char
        encodingChars["escape"] == '\\' as char
        encodingChars["subcomponent"] == '&' as char
    }

    def "getEncodingCharacterMap should use default character when one is missing"() {
        given:
        def customEncodingChars = "_"

        when:
        def encodingChars = HL7Parser.getEncodingCharacterMap(customEncodingChars)

        then:
        encodingChars["field"] == '|' as char
        encodingChars["component"] == '_' as char
        encodingChars["repetition"] == '~' as char
        encodingChars["escape"] == '\\' as char
        encodingChars["subcomponent"] == '&' as char
    }

    def "getEncodingCharacterMap should use custom encoding characters when provided"() {
        given:
        def customEncodingChars = "@#+_"

        when:
        def result = HL7Parser.getEncodingCharacterMap(customEncodingChars)

        then:
        result["field"] == '|' as char
        result["component"] == '@' as char
        result["repetition"] == '#' as char
        result["escape"] == '+' as char
        result["subcomponent"] == '_' as char
    }

    def "parseAndGetValue returns an empty string if a null list of fields is given"() {
        given:
        def nullList = null
        def delimiters = ['|']

        when:
        def out = HL7Parser.parseAndGetValue(nullList, delimiters as char[])

        then:
        out == ""
    }

    def "parseAndGetValue returns an empty string if an empty list of fields is given"() {
        given:
        def emptyList = []
        def delimiters = ['|']

        when:
        def out = HL7Parser.parseAndGetValue(emptyList, delimiters as char[])

        then:
        out == ""
    }

    def "parseAndGetValue returns an empty string if the indices are pointing outside the expected range"() {
        given:
        def emptyList = [
            "MSH|fakeValues",
            "OBR|fakeValues"
        ]
        def delimiters = ['|']

        when:
        def out = HL7Parser.parseAndGetValue(emptyList, delimiters as char[], 10, 20)

        then:
        out == ""
    }

    def "getEncodingCharacterMap uses default definitions when encoding characters are not available"() {
        when:
        def out = HL7Parser.getEncodingCharacterMap("tes")

        then:
        out.size() > 0
    }

    def "getEncodingCharacterMap uses default definitions if the encoding characters are blank"() {
        when:
        def out = HL7Parser.getEncodingCharacterMap(" ")

        then:
        out.size() > 0
    }

    def "getEncodingCharacterMap uses default definitions if the encoding characters are whitespace"() {
        when:
        def out = HL7Parser.getEncodingCharacterMap("")

        then:
        out.size() > 0
    }

    def "getEncodingCharacterMap uses default definitions if the encoding characters are null"() {
        when:
        def out = HL7Parser.getEncodingCharacterMap(null)

        then:
        out.size() > 0
    }
}
