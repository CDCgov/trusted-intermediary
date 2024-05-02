package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.wrappers.HapiFhir
import gov.hhs.cdc.trustedintermediary.wrappers.Logger
import spock.lang.Specification

class TransformationRuleTest extends Specification {

    def mockLogger = Mock(Logger)
    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(Logger, mockLogger)
        TestApplicationContext.injectRegisteredImplementations()
    }

    def "TransformationRule gets and sets correctly"() {
        given:
        def ruleName = "Rule name"
        def ruleDescription = "Rule Description"
        def ruleMessage = "Rule Warning Message"
        def ruleConditions = ["condition1", "condition2"]
        def ruleActions = [
            new TransformationRuleMethod("acton1", null),
            new TransformationRuleMethod("action2", null)
        ]
        TestApplicationContext.register(HapiFhir, Mock(HapiFhir))

        when:
        def rule = new TransformationRule(ruleName, ruleDescription, ruleMessage, ruleConditions, ruleActions)


        then:
        rule.getName() == ruleName
        rule.getDescription() == ruleDescription
        rule.getMessage() == ruleMessage
        rule.getConditions() == ruleConditions
        rule.getRules() == ruleActions
    }

    def "runRule() works correctly"() {
    }
}
