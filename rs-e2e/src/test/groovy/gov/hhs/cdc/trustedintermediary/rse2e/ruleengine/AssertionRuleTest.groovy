package gov.hhs.cdc.trustedintermediary.rse2e.ruleengine

import ca.uhn.hl7v2.model.Message
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiHL7ExpressionEvaluator
import gov.hhs.cdc.trustedintermediary.wrappers.Logger
import spock.lang.Specification

class AssertionRuleTest extends Specification {

    def mockLogger = Mock(Logger)

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(Logger, mockLogger)
        TestApplicationContext.register(HapiHL7ExpressionEvaluator, Mock(HapiHL7ExpressionEvaluator))
        TestApplicationContext.injectRegisteredImplementations()
    }

    def "AssertionRule's properties are set and get correctly"() {
        given:
        def ruleName = "Rule name"
        def conditions = ["condition1", "condition2"]
        def assertions = ["assertion1", "assertion2"]

        when:
        def rule = new AssertionRule(ruleName, conditions, assertions)

        then:
        rule.getName() == ruleName
        rule.getConditions() == conditions
        rule.getRules() == assertions
    }

    def "shouldRun returns expected boolean depending on conditions"() {
        given:
        def mockMessage = Mock(Message)
        def mockEvaluator = Mock(HapiHL7ExpressionEvaluator)
        mockEvaluator.parseAndEvaluate(mockMessage, null, _ as String) >> true >> conditionResult
        TestApplicationContext.register(HapiHL7ExpressionEvaluator, mockEvaluator)

        def rule = new AssertionRule(null, [
            "trueCondition",
            "secondCondition"
        ], null)

        expect:
        rule.shouldRun(mockMessage) == applies

        where:
        conditionResult | applies
        true            | true
        false           | false
    }

    def "shouldRun logs an error and returns false if an exception happens when evaluating a condition"() {
        given:
        def mockMessage = Mock(Message)
        def mockEvaluator = Mock(HapiHL7ExpressionEvaluator)
        mockEvaluator.parseAndEvaluate(mockMessage, null, _ as String) >> { throw new Exception() }
        TestApplicationContext.register(HapiHL7ExpressionEvaluator, mockEvaluator)

        def rule = new AssertionRule(null, ["condition"], null)

        when:
        def applies = rule.shouldRun(mockMessage)

        then:
        1 * mockLogger.logError(_ as String, _ as Exception)
        !applies
    }

    def "runRule returns expected boolean depending on assertions"() {
        given:
        def mockMessage = Mock(Message)
        def mockEvaluator = Mock(HapiHL7ExpressionEvaluator)
        TestApplicationContext.register(HapiHL7ExpressionEvaluator, mockEvaluator)

        def rule = new AssertionRule(null, null, [
            "trueValidation",
            "secondValidation"
        ])

        when:
        mockEvaluator.parseAndEvaluate(mockMessage, _ as Message, _ as String) >> true >> true
        rule.runRule(mockMessage, Mock(Message))

        then:
        0 * mockLogger.logWarning(_ as String)
        0 * mockLogger.logError(_ as String, _ as Exception)

        when:
        mockEvaluator.parseAndEvaluate(mockMessage, _ as Message, _ as String) >> true >> false
        rule.runRule(mockMessage, Mock(Message))

        then:
        1 * mockLogger.logWarning(_ as String)
        0 * mockLogger.logError(_ as String, _ as Exception)
    }

    def "runRule logs an error and returns false if an exception happens when evaluating an assertion"() {
        given:

        def mockMessage = Mock(Message)
        def mockEvaluator = Mock(HapiHL7ExpressionEvaluator)
        mockEvaluator.parseAndEvaluate(mockMessage, _ as Message, _ as String) >> { throw new Exception() }
        TestApplicationContext.register(HapiHL7ExpressionEvaluator, mockEvaluator)

        def rule = new AssertionRule(null, null, ["validation"])

        when:
        rule.runRule(mockMessage, Mock(Message))

        then:
        0 * mockLogger.logWarning(_ as String)
        1 * mockLogger.logError(_ as String, _ as Exception)
    }
}
