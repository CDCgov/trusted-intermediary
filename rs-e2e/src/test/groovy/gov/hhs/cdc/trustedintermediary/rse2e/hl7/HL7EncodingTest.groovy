package gov.hhs.cdc.trustedintermediary.rse2e.hl7

import spock.lang.Specification

class HL7EncodingTest extends Specification  {

    def "defaultEncoding should use expected default characters"() {
        when:
        def encodingChars = HL7Encoding.defaultEncoding()

        then:
        encodingChars.getFieldDelimiter() == '|' as char
        encodingChars.getComponentDelimiter() == '^' as char
        encodingChars.getRepetitionDelimiter() == '~' as char
        encodingChars.getEscapeCharacter() == '\\' as char
        encodingChars.getSubcomponentDelimiter() == '&' as char
    }

    def "fromEncodingField should use custom encoding characters when provided"() {
        given:
        def customEncodingChars = "@#+_"

        when:
        def encodingChars = HL7Encoding.fromEncodingField(customEncodingChars)

        then:
        encodingChars.getFieldDelimiter() == '|' as char
        encodingChars.getComponentDelimiter() == '@' as char
        encodingChars.getRepetitionDelimiter() == '#' as char
        encodingChars.getEscapeCharacter() == '+' as char
        encodingChars.getSubcomponentDelimiter() == '_' as char
    }

    def "fromEncodingField should use default characters when encoding characters passed is blank or null"() {
        when:
        def blankEncodingChars = HL7Encoding.fromEncodingField("")

        then:
        blankEncodingChars.getFieldDelimiter() == '|' as char
        blankEncodingChars.getComponentDelimiter() == '^' as char
        blankEncodingChars.getRepetitionDelimiter() == '~' as char
        blankEncodingChars.getEscapeCharacter() == '\\' as char
        blankEncodingChars.getSubcomponentDelimiter() == '&' as char

        when:
        def nullEncodingChars = HL7Encoding.fromEncodingField(null)

        then:
        nullEncodingChars.getFieldDelimiter() == '|' as char
        nullEncodingChars.getComponentDelimiter() == '^' as char
        nullEncodingChars.getRepetitionDelimiter() == '~' as char
        nullEncodingChars.getEscapeCharacter() == '\\' as char
        nullEncodingChars.getSubcomponentDelimiter() == '&' as char
    }
}
