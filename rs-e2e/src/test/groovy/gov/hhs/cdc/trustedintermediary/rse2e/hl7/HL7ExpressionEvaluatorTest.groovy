package gov.hhs.cdc.trustedintermediary.rse2e.hl7

import gov.hhs.cdc.trustedintermediary.wrappers.HealthData
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import spock.lang.Specification

class HL7ExpressionEvaluatorTest extends Specification {

    def evaluator = HL7ExpressionEvaluator.getInstance()

    HL7Message hl7Message
    String messageContent

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(HL7ExpressionEvaluator, evaluator)

        messageContent = """MSH|^~\\&|Sender Application^sender.test.com^DNS|Sender Facility^0.0.0.0.0.0.0.0^ISO|Receiver Application^0.0.0.0.0.0.0.0^ISO|Receiver Facility^simulated-lab-id^DNS|20230101010000-0000||ORM^O01^ORM_O01|111111|T|2.5.1
PID|1||11102779^^^CR^MR||SMITH^BB SARAH^^^^^L"""
        hl7Message = HL7Message.parse(messageContent)

        TestApplicationContext.injectRegisteredImplementations()
    }

    def "evaluateExpression returns boolean when evaluating valid assertions"() {
        given:
        def spyEvaluator = Spy(HL7ExpressionEvaluator.getInstance())
        spyEvaluator.getLiteralOrFieldValue(_ as HL7Message, _ as HL7Message, _ as String) >> "mockedValue"
        spyEvaluator.evaluateEquality(_ as String, _ as String, _ as String) >> true
        spyEvaluator.evaluateMembership(_ as String, _ as String) >> true
        spyEvaluator.evaluateCollectionCount(_ as HL7Message, _ as String, _ as String, _ as String) >> true
        def healthData = Mock(HL7Message)

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
        spyEvaluator.getLiteralOrFieldValue(_ as HL7Message, null, _ as String) >> "mockedValue"
        spyEvaluator.evaluateEquality(_ as String, _ as String, _ as String) >> true
        spyEvaluator.evaluateMembership(_ as String, _ as String) >> true
        spyEvaluator.evaluateCollectionCount(_ as HL7Message, _ as String, _ as String, _ as String) >> true
        def healthData = Mock(HL7Message)

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
        def e = thrown(HL7ParserException)
        e.getMessage().contains("Invalid statement format")
    }

    def "evaluateExpression should throw exception for more than two messages"() {
        when:
        evaluator.evaluateExpression("'EPIC' = 'EPIC'", Mock(HealthData), Mock(HealthData), Mock(HealthData))

        then:
        def e = thrown(HL7ParserException)
        e.getMessage().contains("Expected two messages")
    }

    def "evaluateExpression should throw exception when there is no operator"() {
        given:
        def condition = "input.MSH-3"

        when:
        evaluator.evaluateExpression(condition, Mock(HealthData))

        then:
        def e = thrown(HL7ParserException)
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
        def e = thrown(HL7ParserException)
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
        def e = thrown(HL7ParserException)
        e.getMessage().contains("Invalid collection format")
    }

    def "evaluateCollectionCount returns true when segment count matches desired count"() {
        given:
        def rightOperand = "1"
        def segmentName = "MSH"
        def operator = "="

        when:
        def result = evaluator.evaluateCollectionCount(hl7Message, segmentName, rightOperand, operator)

        then:
        result
    }

    def "evaluateCollectionCount returns false when segment count does not match desired count"() {
        given:
        def rightOperand = "3"
        def segmentName = "MSH"
        def operator = "="

        when:
        def result = evaluator.evaluateCollectionCount(hl7Message, segmentName, rightOperand, operator)

        then:
        !result
    }

    def "evaluateCollectionCount throws exception when segment count is not numeric"() {
        given:
        def rightOperand = "three"
        def segmentName = "MSH"
        def operator = "="

        when:
        evaluator.evaluateCollectionCount(hl7Message, segmentName, rightOperand, operator)

        then:
        def e = thrown(HL7ParserException)
        e.getCause().getClass() == NumberFormatException
    }

    def "evaluateCollectionCount evaluates correctly when specified segment is not in message"() {
        given:
        def rightOperand = "3"
        def segmentName = "OBX"
        def operator = "="

        when:
        def result = evaluator.evaluateCollectionCount(hl7Message, segmentName, rightOperand, operator)

        then:
        !result
    }

    def "getLiteralOrFieldValue returns literal value when literal is specified"() {
        given:
        def operand = "'Epic'"
        def inputMessage = Mock(HL7Message)
        def outputMessage = Mock(HL7Message)

        when:
        def result = evaluator.getLiteralOrFieldValue(outputMessage, inputMessage, operand)

        then:
        result == "Epic"
    }

    def "getLiteralOrFieldValue returns field value when field is specified"() {
        given:
        def operand = "MSH-3"
        def inputMessage = Mock(HL7Message)
        def msh3 = "Sender Application^sender.test.com^DNS"

        when:
        def result = evaluator.getLiteralOrFieldValue(hl7Message, inputMessage, operand)

        then:
        result == msh3
    }

    def "getLiteralOrFieldValue throws exception when using invalid operand"() {
        given:
        def operand = "invalidField"
        def inputMessage = Mock(HL7Message)

        when:
        evaluator.getLiteralOrFieldValue(hl7Message, inputMessage, operand)

        then:
        thrown(HL7ParserException)
    }

    def "getFieldValue returns specified field value"() {
        given:
        def fieldName = "MSH-3"
        def msh3 = "Sender Application^sender.test.com^DNS"
        def inputMessage = Mock(HL7Message)

        when:
        def result = evaluator.getFieldValue(hl7Message, inputMessage, fieldName)

        then:
        result == msh3
    }

    def "getFieldValue throws exception for non numeric field index"() {
        given:
        def fieldName = "MSH-three"
        def inputMessage = Mock(HL7Message)

        when:
        evaluator.getFieldValue(hl7Message, inputMessage, fieldName)

        then:
        thrown(HL7ParserException)
    }

    def "getFieldValue throws exception for empty field name"() {
        given:
        def fieldName = ""
        def inputMessage = Mock(HL7Message)

        when:
        evaluator.getFieldValue(hl7Message, inputMessage, fieldName)

        then:
        thrown(HL7ParserException)
    }

    def "getMessageBySource should return input message when source is input"() {
        given:
        def source = "input"
        def inputMessage = Mock(HL7Message)
        def outputMessage = Mock(HL7Message)

        when:
        def result = evaluator.getMessageBySource(source, inputMessage, outputMessage)

        then:
        result == inputMessage
    }

    def "getMessageBySource should return output message when source is not input"() {
        given:
        def source = "output"
        def inputMessage = Mock(HL7Message)
        def outputMessage = Mock(HL7Message)

        when:
        def result = evaluator.getMessageBySource(source, inputMessage, outputMessage)

        then:
        result == outputMessage
    }

    def "getMessageBySource should return output message when source is empty"() {
        given:
        def source = ""
        def inputMessage = Mock(HL7Message)
        def outputMessage = Mock(HL7Message)

        when:
        def result = evaluator.getMessageBySource(source, inputMessage, outputMessage)

        then:
        result == outputMessage
    }

    def "getMessageBySource should throw exception when source is input and input message is null"() {
        given:
        def source = "input"
        def inputMessage = null
        def outputMessage = Mock(HL7Message)

        when:
        evaluator.getMessageBySource(source, inputMessage, outputMessage)

        then:
        def e = thrown(HL7ParserException)
        e.getMessage().contains("Input message is null for")
    }
}
