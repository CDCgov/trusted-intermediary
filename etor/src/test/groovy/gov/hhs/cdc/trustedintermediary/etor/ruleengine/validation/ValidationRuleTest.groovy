package gov.hhs.cdc.trustedintermediary.etor.ruleengine.validation

import gov.hhs.cdc.trustedintermediary.HealthDataMock
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiFhirImplementation
import gov.hhs.cdc.trustedintermediary.wrappers.HealthData
import gov.hhs.cdc.trustedintermediary.wrappers.Logger
import gov.hhs.cdc.trustedintermediary.wrappers.HealthDataExpressionEvaluator
import spock.lang.Specification

class ValidationRuleTest extends Specification {

    def mockLogger = Mock(Logger)

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(Logger, mockLogger)
        TestApplicationContext.injectRegisteredImplementations()
    }

    def "ValidationRule's properties are set and get correctly"() {
        given:
        def ruleName = "Rule name"
        def ruleDescription = "Rule Description"
        def ruleWarningMessage = "Rule Warning Message"
        def conditions = ["condition1", "condition2"]
        def validations = ["validation1", "validation2"]
        TestApplicationContext.register(HealthDataExpressionEvaluator, Mock(HealthDataExpressionEvaluator))
        TestApplicationContext.injectRegisteredImplementations()

        when:
        def rule = new ValidationRule(ruleName, ruleDescription, ruleWarningMessage, conditions, validations)

        then:
        rule.getName() == ruleName
        rule.getDescription() == ruleDescription
        rule.getMessage() == ruleWarningMessage
        rule.getConditions() == conditions
        rule.getRules() == validations
    }

    def "shouldRun returns expected boolean depending on conditions"() {
        given:
        def mockFhir = Mock(HapiFhirImplementation)
        mockFhir.evaluateExpression(_ as String, _ as HealthData) >> true >> conditionResult
        TestApplicationContext.register(HealthDataExpressionEvaluator, mockFhir)

        def rule = new ValidationRule(null, null, null, [
            "trueCondition",
            "secondCondition"
        ], null)

        expect:
        rule.shouldRun(Mock(HealthData)) == applies

        where:
        conditionResult | applies
        true            | true
        false           | false
    }

    def "shouldRun logs an error and returns false if an exception happens when evaluating a condition"() {
        given:
        def mockFhir = Mock(HapiFhirImplementation)
        mockFhir.evaluateExpression("condition", _ as HealthData) >> { throw new Exception() }
        TestApplicationContext.register(HealthDataExpressionEvaluator, mockFhir)

        def rule = new ValidationRule(null, null, null, ["condition"], null)

        when:
        def applies = rule.shouldRun(Mock(HealthData))

        then:
        1 * mockLogger.logError(_ as String, _ as Exception)
        !applies
    }

    def "runRule returns expected boolean depending on validations"() {
        given:
        def mockFhir = Mock(HapiFhirImplementation)
        TestApplicationContext.register(HealthDataExpressionEvaluator, mockFhir)

        def rule = new ValidationRule(null, null, null, null, [
            "trueValidation",
            "secondValidation"
        ])

        when:
        mockFhir.evaluateExpression(_ as String, _ as HealthData)  >> true >> true
        rule.runRule(new HealthDataMock("resource"))

        then:
        0 * mockLogger.logWarning(_ as String)
        0 * mockLogger.logError(_ as String, _ as Exception)

        when:
        mockFhir.evaluateExpression(_ as String, _ as HealthData) >> true >> false
        rule.runRule(new HealthDataMock("resource"))

        then:
        1 * mockLogger.logWarning(_ as String)
        0 * mockLogger.logError(_ as String, _ as Exception)
    }

    def "runRule logs an error if an exception happens when evaluating a validation"() {
        given:
        def mockFhir = Mock(HapiFhirImplementation)
        mockFhir.evaluateExpression(_ as String, _ as HealthData) >> { throw new Exception() }
        TestApplicationContext.register(HealthDataExpressionEvaluator, mockFhir)

        def rule = new ValidationRule(null, null, null, null, ["validation"])

        when:
        rule.runRule(Mock(HealthData))

        then:
        0 * mockLogger.logWarning(_ as String)
        1 * mockLogger.logError(_ as String, _ as Exception)
    }

    def "runRule logs an error if passing more than one HealthData"() {
        given:
        def mockFhir = Mock(HapiFhirImplementation)
        TestApplicationContext.register(HealthDataExpressionEvaluator, mockFhir)

        def rule = new ValidationRule(null, null, null, ["condition"], ["validation"])

        when:
        rule.runRule(Mock(HealthData), Mock(HealthData))

        then:
        1 * mockLogger.logError(_ as String)
    }
}
