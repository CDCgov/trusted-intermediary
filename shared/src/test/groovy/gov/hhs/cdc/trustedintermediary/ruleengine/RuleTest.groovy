package gov.hhs.cdc.trustedintermediary.ruleengine

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.wrappers.HealthData
import gov.hhs.cdc.trustedintermediary.wrappers.Logger
import spock.lang.Specification

class RuleTest extends Specification {

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(Logger, Mock(Logger))
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
        mockHealthData.evaluateCondition(_ as String) >> true >> conditionResult
        TestApplicationContext.register(HealthData, mockHealthData)

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
        def mockLogger = Mock(Logger)
        def mockHealthData = Mock(HealthData)
        mockHealthData.evaluateCondition("condition") >> { throw new Exception() }
        TestApplicationContext.register(Logger, mockLogger)
        TestApplicationContext.register(HealthData, mockHealthData)

        def rule = new Rule(null, null, null, ["condition"], null)

        when:
        def applies = rule.shouldRun(Mock(HealthData))

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
