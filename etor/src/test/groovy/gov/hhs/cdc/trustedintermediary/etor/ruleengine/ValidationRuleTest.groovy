package gov.hhs.cdc.trustedintermediary.etor.ruleengine

import gov.hhs.cdc.trustedintermediary.FhirResourceMock
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiFhirImplementation
import gov.hhs.cdc.trustedintermediary.wrappers.HapiFhir
import gov.hhs.cdc.trustedintermediary.wrappers.Logger
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
        TestApplicationContext.register(HapiFhir, Mock(HapiFhir))

        when:
        def rule = new ValidationRule(ruleName, ruleDescription, ruleWarningMessage, conditions, validations)

        then:
        rule.getName() == ruleName
        rule.getDescription() == ruleDescription
        rule.getViolationMessage() == ruleWarningMessage
        rule.getConditions() == conditions
        rule.getValidations() == validations
    }

    def "appliesTo returns expected boolean depending on conditions"() {
        given:
        def mockFhir = Mock(HapiFhir)
        mockFhir.evaluateCondition(_ as Object, _ as String) >> true >> conditionResult
        TestApplicationContext.register(HapiFhir, mockFhir)

        def rule = new ValidationRule(null, null, null, [
            "trueCondition",
            "secondCondition"
        ], null)

        expect:
        rule.appliesTo(new FhirResourceMock("resource")) == applies

        where:
        conditionResult | applies
        true            | true
        false           | false
    }

    def "appliesTo logs an error and returns false if an exception happens when evaluating a condition"() {
        given:
        def mockFhir = Mock(HapiFhirImplementation)
        mockFhir.evaluateCondition(_ as Object, "condition") >> { throw new Exception() }
        TestApplicationContext.register(HapiFhir, mockFhir)

        def rule = new ValidationRule(null, null, null, ["condition"], null)

        when:
        def applies = rule.appliesTo(Mock(FhirResource))

        then:
        1 * mockLogger.logError(_ as String, _ as Exception)
        !applies
    }

    def "isValid returns expected boolean depending on validations"() {
        given:
        def mockFhir = Mock(HapiFhir)
        mockFhir.evaluateCondition(_ as Object, _ as String) >> true >> validationResult
        TestApplicationContext.register(HapiFhir, mockFhir)

        def rule = new ValidationRule(null, null, null, null, [
            "trueValidation",
            "secondValidation"
        ])

        expect:
        rule.isValid(new FhirResourceMock("resource")) == valid

        where:
        validationResult | valid
        true             | true
        false            | false
    }

    def "isValid logs an error and returns false if an exception happens when evaluating a validation"() {
        given:
        def mockFhir = Mock(HapiFhirImplementation)
        mockFhir.evaluateCondition(_ as Object, "condition") >> { throw new Exception() }
        TestApplicationContext.register(HapiFhir, mockFhir)

        def rule = new ValidationRule(null, null, null, null, ["validation"])

        when:
        def valid = rule.isValid(Mock(FhirResource))

        then:
        1 * mockLogger.logError(_ as String, _ as Exception)
        !valid
    }
}
