package gov.hhs.cdc.trustedintermediary.ruleengine

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.wrappers.HealthData
import gov.hhs.cdc.trustedintermediary.wrappers.HealthDataExpressionEvaluator
import gov.hhs.cdc.trustedintermediary.wrappers.Logger
import spock.lang.Specification

class RuleTest extends Specification {

    def mockLogger = Mock(Logger)

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(Logger, mockLogger)
        TestApplicationContext.register(HealthDataExpressionEvaluator, Mock(HealthDataExpressionEvaluator))
        TestApplicationContext.injectRegisteredImplementations()
    }

    def "Rule's properties are set and get correctly"() {
        given:
        def ruleName = "Rule name"
        def ruleDescription = "Rule Description"
        def ruleWarningMessage = "Rule Warning Message"
        def conditions = ["condition1", "condition2"]
        def validations = ["validation1", "validation2"]

        when:
        def rule = new Rule(ruleName, ruleDescription, ruleWarningMessage, conditions, validations)

        then:
        rule.getName() == ruleName
        rule.getDescription() == ruleDescription
        rule.getMessage() == ruleWarningMessage
        rule.getConditions() == conditions
        rule.getRules() == validations
    }

    def "shouldRun returns expected boolean depending on conditions"() {
        given:
        def mockHealthData = Mock(HealthData)
        def mockEvaluator = Mock(HealthDataExpressionEvaluator)
        mockEvaluator.evaluateExpression(_ as String, mockHealthData) >> true >> conditionResult
        TestApplicationContext.register(HealthDataExpressionEvaluator, mockEvaluator)
        TestApplicationContext.injectRegisteredImplementations()

        def rule = new Rule(null, null, null, [
            "trueCondition",
            "secondCondition"
        ], null)

        expect:
        rule.shouldRun(mockHealthData) == applies

        where:
        conditionResult | applies
        true            | true
        false           | false
    }

    def "shouldRun logs an error and returns false if an exception happens when evaluating a condition"() {
        given:
        def mockHealthData = Mock(HealthData)
        def mockEvaluator = Mock(HealthDataExpressionEvaluator)
        mockEvaluator.evaluateExpression(_ as String, mockHealthData) >> { throw new Exception() }
        TestApplicationContext.register(HealthDataExpressionEvaluator, mockEvaluator)
        TestApplicationContext.injectRegisteredImplementations()

        def rule = new Rule(null, null, null, ["condition"], null)

        when:
        def applies = rule.shouldRun(mockHealthData)

        then:
        1 * mockLogger.logError(_ as String, _ as Exception)
        !applies
    }

    def "runRule throws an UnsupportedOperationException when ran from the Rule class"() {
        given:
        def rule = new Rule()

        when:
        rule.runRule(Mock(HealthData))

        then:
        thrown(UnsupportedOperationException)
    }
}
