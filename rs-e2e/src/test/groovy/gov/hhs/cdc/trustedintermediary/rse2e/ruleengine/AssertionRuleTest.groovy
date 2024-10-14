package gov.hhs.cdc.trustedintermediary.rse2e.ruleengine

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.wrappers.HealthData
import gov.hhs.cdc.trustedintermediary.wrappers.Logger
import gov.hhs.cdc.trustedintermediary.wrappers.HealthDataExpressionEvaluator
import spock.lang.Specification

class AssertionRuleTest extends Specification {

    def mockLogger = Mock(Logger)
    def mockEvaluator = Mock(HealthDataExpressionEvaluator)

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(Logger, mockLogger)
        TestApplicationContext.register(HealthDataExpressionEvaluator, mockEvaluator)
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
        def mockData = Mock(HealthData)
        mockEvaluator.evaluateExpression(_ as String, mockData) >> true >> conditionResult

        def rule = new AssertionRule(null, [
            "trueCondition",
            "secondCondition"
        ], null)

        expect:
        rule.shouldRun(mockData) == applies

        where:
        conditionResult | applies
        true            | true
        false           | false
    }

    def "shouldRun logs an error and returns false if an exception happens when evaluating a condition"() {
        given:
        def mockData = Mock(HealthData)
        mockEvaluator.evaluateExpression(_ as String, mockData) >> { throw new Exception() }

        def rule = new AssertionRule(null, ["condition"], null)

        when:
        def applies = rule.shouldRun(mockData)

        then:
        1 * mockLogger.logError(_ as String, _ as Exception)
        !applies
    }

    def "runRule returns expected boolean depending on assertions"() {
        given:
        def mockData = Mock(HealthData)

        def rule = new AssertionRule(null, null, [
            "trueValidation",
            "secondValidation"
        ])

        when:
        mockEvaluator.evaluateExpression(_ as String, mockData, _ as HealthData) >> true >> true
        rule.runRule(mockData, Mock(HealthData))

        then:
        0 * mockLogger.logWarning(_ as String)
        0 * mockLogger.logError(_ as String, _ as Exception)

        when:
        mockEvaluator.evaluateExpression(_ as String, mockData, _ as HealthData) >> true >> false
        rule.runRule(mockData, Mock(HealthData))

        then:
        1 * mockLogger.logWarning(_ as String)
        0 * mockLogger.logError(_ as String, _ as Exception)
    }

    def "runRule logs an error and returns false if an exception happens when evaluating an assertion"() {
        given:

        def mockData = Mock(HealthData)
        mockEvaluator.evaluateExpression(_ as String, mockData, _ as HealthData) >> { throw new Exception() }

        def rule = new AssertionRule(null, null, ["validation"])

        when:
        rule.runRule(mockData, Mock(HealthData))

        then:
        0 * mockLogger.logWarning(_ as String)
        1 * mockLogger.logError(_ as String, _ as Exception)
    }
}
