package gov.hhs.cdc.trustedintermediary.rse2e.hl7


import ca.uhn.hl7v2.model.Message
import gov.hhs.cdc.trustedintermediary.wrappers.HealthData
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import spock.lang.Specification

class HL7ExpressionEvaluatorTest extends Specification {

    def evaluator = HL7ExpressionEvaluator.getInstance()

    char hl7FieldSeparator = '|'
    String hl7FieldEncodingCharacters = "^~\\&"
    HL7Message mshMessage
    String mshSegmentText

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(HL7ExpressionEvaluator, evaluator)

        mshSegmentText = "MSH|^~\\&|Sender Application^sender.test.com^DNS|Sender Facility^0.0.0.0.0.0.0.0^ISO|Receiver Application^0.0.0.0.0.0.0.0^ISO|Receiver Facility^simulated-lab-id^DNS|20230101010000-0000||ORM^O01^ORM_O01|111111|T|2.5.1"

        def segments = [
            MSH: [
                "|",
                "^~\\&",
                "Sender Application^sender.test.com^DNS",
                "Sender Facility^0.0.0.0.0.0.0.0^ISO",
                "Receiver Application^0.0.0.0.0.0.0.0^ISO",
                "Receiver Facility^simulated-lab-id^DNS",
                "20230101010000-0000",
                "",
                "ORM^O01^ORM_O01",
                "111111",
                "T",
                "2.5.1"
            ]
        ]

        mshMessage = new HL7Message(segments, "|^~\\&" as Map<String, Character>)

        TestApplicationContext.injectRegisteredImplementations()
    }

    def "evaluateExpression returns boolean when evaluating valid assertions"() {
        given:
        def spyEvaluator = Spy(HL7ExpressionEvaluator.getInstance())
        spyEvaluator.getLiteralOrFieldValue(_ as Message, _ as Message, _ as String) >> "mockedValue"
        spyEvaluator.evaluateEquality(_ as String, _ as String, _ as String) >> true
        spyEvaluator.evaluateMembership(_ as String, _ as String) >> true
        spyEvaluator.evaluateCollectionCount(_ as Message, _ as String, _ as String, _ as String) >> true

        def healthData = Mock(HealthData) {
            getUnderlyingData() >> Mock(Message)
        }

        expect:
        spyEvaluator.evaluateExpression(assertion, healthData, healthData)

        where:
        assertion                     | _
        "input.MSH-1 = MSH-1"         | _
        "input.MSH-1 = input.MSH-1"   | _
        "MSH-1 = input.MSH-1"         | _
        "output.MSH-1 = MSH-1"        | _
        "output.MSH-1 = output.MSH-1" | _
        "MSH-1 = output.MSH-1"        | _
        "input.MSH-1 = output.MSH-1"  | _
        "output.MSH-1 = input.MSH-1"  | _
        "MSH-9.1 = 'R01'"             | _
        "MSH-9.1 = 'R01'"             | _
        "MSH-6 in ('R797', 'R508')"   | _
        "OBR.count() = 1"             | _
    }

    def "evaluateExpression allows null input message when no assertions use input"() {
        given:
        def spyEvaluator = Spy(HL7ExpressionEvaluator.getInstance())
        spyEvaluator.getLiteralOrFieldValue(_ as Message, null, _ as String) >> "mockedValue"
        spyEvaluator.evaluateEquality(_ as String, _ as String, _ as String) >> true
        spyEvaluator.evaluateMembership(_ as String, _ as String) >> true
        spyEvaluator.evaluateCollectionCount(_ as Message, _ as String, _ as String, _ as String) >> true

        def healthData = Mock(HealthData) {
            getUnderlyingData() >> Mock(Message)
        }

        expect:
        spyEvaluator.evaluateExpression(assertion, healthData)

        where:
        assertion                     | _
        "MSH-1 = MSH-1"               | _
        "output.MSH-1 = MSH-1"        | _
        "MSH-9.1 = 'R01'"             | _
        "MSH-6 in ('R797', 'R508')"   | _
        "OBR.count() = 1"             | _
    }

    def "evaluateExpression should throw exception for invalid expression format"() {
        when:
        evaluator.evaluateExpression("invalid format", Mock(HealthData))

        then:
        def e = thrown(IllegalArgumentException)
        e.getMessage().contains("Invalid statement format")
    }

    def "evaluateExpression should throw exception for more than two messages"() {
        when:
        evaluator.evaluateExpression("'EPIC' = 'EPIC'", Mock(HealthData), Mock(HealthData), Mock(HealthData))

        then:
        def e = thrown(IllegalArgumentException)
        e.getMessage().contains("Expected two messages")
    }

    def "evaluateExpression should throw exception when there is no operator"() {
        given:
        def condition = "input.MSH-3"

        when:
        evaluator.evaluateExpression(condition, Mock(HealthData))

        then:
        def e = thrown(IllegalArgumentException)
        e.getMessage().contains("Invalid statement format")
    }

    def "evaluateEquality should evaluate values in evaluateEquality correctly"() {
        given:
        boolean result

        when:
        result = evaluator.evaluateEquality("'EPIC'", "'EPIC'", "=")

        then:
        result

        when:
        result = evaluator.evaluateEquality("'EPIC'", "'othervalue'", "=")

        then:
        !result

        when:
        result = evaluator.evaluateEquality("'EPIC'", "'CERNER'", "!=")

        then:
        result

        when:
        result = evaluator.evaluateEquality("'EPIC'", "'EPIC'", "!=")

        then:
        !result
    }

    def "evaluateEquality should throw exception when the operator in evaluateEquality is not valid"() {
        given:
        def unknownOperator = "<>"

        when:
        evaluator.evaluateEquality("left", "right", unknownOperator)

        then:
        def e = thrown(IllegalArgumentException)
        e.getMessage().contains("Unknown operator")
    }

    def "evaluateMembership returns boolean when evaluating valid assertions"() {
        given:
        def expectedValue = "expectedValue"

        when:
        def expectedValueInSet = "('expectedValue', 'value2')"
        def expectedValueInSetResult = evaluator.evaluateMembership(expectedValue, expectedValueInSet)

        then:
        expectedValueInSetResult

        when:
        def expectedValueNotInSet = "('value1', 'value2')"
        def expectedValueNotInSetResult = evaluator.evaluateMembership(expectedValue, expectedValueNotInSet)

        then:
        !expectedValueNotInSetResult
    }

    def "evaluateMembership throws exception when the set is not valid"() {
        given:
        def invalidSet = "invalidSet"

        when:
        evaluator.evaluateMembership("value", invalidSet)

        then:
        def e = thrown(IllegalArgumentException)
        e.getMessage().contains("Invalid collection format")
    }

    def "evaluateCollectionCount returns true when segment count matches desired count"() {
        given:
        def rightOperand = "1"
        def segmentName = "MSH"
        def operator = "="

        when:
        def result = evaluator.evaluateCollectionCount(mshMessage, segmentName, rightOperand, operator)

        then:
        result
    }

    def "evaluateCollectionCount returns false when segment count does not match desired count"() {
        given:
        def rightOperand = "3"
        def segmentName = "MSH"
        def operator = "="

        when:
        def result = evaluator.evaluateCollectionCount(mshMessage, segmentName, rightOperand, operator)

        then:
        !result
    }

    def "evaluateCollectionCount throws exception when segment count is not numeric"() {
        given:
        def rightOperand = "three"
        def segmentName = "MSH"
        def operator = "="

        when:
        evaluator.evaluateCollectionCount(mshMessage, segmentName, rightOperand, operator)

        then:
        def e = thrown(IllegalArgumentException)
        e.getCause().getClass() == NumberFormatException
    }

    def "evaluateCollectionCount evaluates correctly when specified segment is not in message"() {
        given:
        def rightOperand = "3"
        def segmentName = "OBX"
        def operator = "="

        when:
        def result = evaluator.evaluateCollectionCount(mshMessage, segmentName, rightOperand, operator)

        then:
        !result
    }

    def "getLiteralOrFieldValue returns literal value when literal is specified"() {
        given:
        def operand = "'Epic'"
        def inputMessage = Mock(Message)
        def outputMessage = Mock(Message)

        when:
        def result = evaluator.getLiteralOrFieldValue(outputMessage, inputMessage, operand)

        then:
        result == "Epic"
    }

    def "getLiteralOrFieldValue returns field value when field is specified"() {
        given:
        def operand = "MSH-3"
        def inputMessage = Mock(Message)
        def msh3 = "Sender Application^sender.test.com^DNS"

        when:
        def result = evaluator.getLiteralOrFieldValue(mshMessage, inputMessage, operand)

        then:
        result == msh3
    }

    def "getFieldValue returns specified field value"() {
        given:
        def fieldName = "MSH-3"
        def msh3 = "Sender Application^sender.test.com^DNS"
        def inputMessage = Mock(Message)

        when:
        def result = evaluator.getFieldValue(mshMessage, inputMessage, fieldName)

        then:
        result == msh3
    }

    def "getFieldValue throws exception for non numeric field index"() {
        given:
        def fieldName = "MSH-three"
        def inputMessage = Mock(Message)

        when:
        evaluator.getFieldValue(mshMessage, inputMessage, fieldName)

        then:
        def e = thrown(IllegalArgumentException)
        e.getCause().getClass() == NumberFormatException
    }

    def "getFieldValue throws exception for empty field name"() {
        given:
        def fieldName = ""
        def inputMessage = Mock(Message)

        when:
        evaluator.getFieldValue(mshMessage, inputMessage, fieldName)

        then:
        def e = thrown(IllegalArgumentException)
        e.getMessage().contains("Invalid field name format")
    }

    def "getSegmentFieldValue should return segment when field components indicate segment"() {
        given:
        def fieldName = "MSH"

        when:
        def result = evaluator.getSegmentFieldValue(mshSegmentText, fieldName, hl7FieldSeparator, hl7FieldEncodingCharacters)

        then:
        result == mshSegmentText
    }

    def "getSegmentFieldValue should return field when field components indicate field"() {
        given:
        def fieldName = "MSH-3"
        def msh3 = "Sender Application^sender.test.com^DNS"

        when:
        def result = evaluator.getSegmentFieldValue(mshSegmentText, fieldName, hl7FieldSeparator, hl7FieldEncodingCharacters)

        then:
        result == msh3
    }

    def "getSegmentFieldValue should return subfield when field components indicate subfield"() {
        given:
        def fieldName = "MSH-3.2"
        def msh32 = "sender.test.com"

        when:
        def result = evaluator.getSegmentFieldValue(mshSegmentText, fieldName, hl7FieldSeparator, hl7FieldEncodingCharacters)

        then:
        result == msh32
    }

    def "getSegmentFieldValue should return empty string when field components indicate subfield but subfield not present"() {
        given:
        def fieldName = "MSH-3.4"

        when:
        def result = evaluator.getSegmentFieldValue(mshSegmentText, fieldName, hl7FieldSeparator, hl7FieldEncodingCharacters)

        then:
        result == ""
    }

    def "getSegmentFieldValue returns null when looking for segment that isn't in message"() {
        given:
        def fieldName = "OBX"

        when:
        def result = evaluator.getSegmentFieldValue(mshSegmentText, fieldName, hl7FieldSeparator, hl7FieldEncodingCharacters)

        then:
        result == null
    }

    def "getSegmentFieldValue throws exception when field name is invalid"() {
        given:
        def fieldName = "MSH-"

        when:
        evaluator.getSegmentFieldValue(mshSegmentText, fieldName, hl7FieldSeparator, hl7FieldEncodingCharacters)

        then:
        def e = thrown(IllegalArgumentException)
        e.getMessage().contains("Invalid HL7 field format: ")
    }

    def "getSegmentFieldValue throws exception when field index is out of bounds"() {
        given:
        def fieldName = "MSH-99"

        when:
        evaluator.getSegmentFieldValue(mshSegmentText, fieldName, hl7FieldSeparator, hl7FieldEncodingCharacters)

        then:
        def e = thrown(IllegalArgumentException)
        e.getMessage().contains("Invalid field index (out of bounds)")
    }

    def "getSegmentFieldValue throws exception when there are too many subfield levels"() {
        given:
        def fieldName = "MSH-3.3.3.3.3.3"

        when:
        evaluator.getSegmentFieldValue(mshSegmentText, fieldName, hl7FieldSeparator, hl7FieldEncodingCharacters)

        then:
        def e = thrown(IllegalArgumentException)
        e.getMessage().contains("Invalid subfield index (out of bounds)")
    }

    def "getSegmentFieldValue returns empty string when sub-field index is out of bounds"() {
        given:
        def fieldName = "MSH-3.99"

        when:
        def result = evaluator.getSegmentFieldValue(mshSegmentText, fieldName, hl7FieldSeparator, hl7FieldEncodingCharacters)

        then:
        result == ""
    }

    def "getMessageBySource should return input message when source is input"() {
        given:
        def source = "input"
        def inputMessage = Mock(Message)
        def outputMessage = Mock(Message)

        when:
        def result = evaluator.getMessageBySource(source, inputMessage, outputMessage)

        then:
        result == inputMessage
    }

    def "getMessageBySource should return output message when source is not input"() {
        given:
        def source = "output"
        def inputMessage = Mock(Message)
        def outputMessage = Mock(Message)

        when:
        def result = evaluator.getMessageBySource(source, inputMessage, outputMessage)

        then:
        result == outputMessage
    }

    def "getMessageBySource should return output message when source is empty"() {
        given:
        def source = ""
        def inputMessage = Mock(Message)
        def outputMessage = Mock(Message)

        when:
        def result = evaluator.getMessageBySource(source, inputMessage, outputMessage)

        then:
        result == outputMessage
    }

    def "getMessageBySource should throw exception when source is input and input message is null"() {
        given:
        def source = "input"
        def inputMessage = null
        def outputMessage = Mock(Message)

        when:
        evaluator.getMessageBySource(source, inputMessage, outputMessage)

        then:
        def e = thrown(IllegalArgumentException)
        e.getMessage().contains("Input message is null for")
    }
}
