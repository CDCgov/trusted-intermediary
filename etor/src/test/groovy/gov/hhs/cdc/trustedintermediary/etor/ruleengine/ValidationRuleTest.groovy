package gov.hhs.cdc.trustedintermediary.etor.ruleengine

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiFhirImplementation
import gov.hhs.cdc.trustedintermediary.wrappers.HapiFhir
import gov.hhs.cdc.trustedintermediary.wrappers.Logger
import org.hl7.fhir.instance.model.api.IBaseResource
import org.hl7.fhir.r4.model.Bundle
import spock.lang.Specification

class ValidationRuleTest extends Specification {

    def mockLogger = Mock(Logger)

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(HapiFhir, Mock(HapiFhirImplementation))
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

        when:
        def rule = new ValidationRule(ruleName, ruleDescription, ruleWarningMessage, conditions, validations)

        then:
        rule.getName() == ruleName
        rule.getDescription() == ruleDescription
        rule.getWarningMessage() == ruleWarningMessage
        rule.getConditions() == conditions
        rule.getValidations() == validations
    }

    def "appliesTo returns expected boolean depending on conditions"() {
        given:
        def trueCondition = "trueCondition"
        IBaseResource fhirResource = new Bundle()

        def mockFhir = Mock(HapiFhir)
        mockFhir.evaluateCondition(fhirResource, trueCondition) >> true
        mockFhir.evaluateCondition(fhirResource, secondCondition) >> conditionResult
        TestApplicationContext.register(HapiFhir, mockFhir)
        TestApplicationContext.injectRegisteredImplementations()

        def rule = new ValidationRule(null, null, null, [
            trueCondition,
            secondCondition
        ], null)

        expect:
        rule.appliesTo(fhirResource) == applies

        where:
        secondCondition   | conditionResult | applies
        "secondCondition" | true            | true
        "secondCondition" | false           | false
    }

    def "appliesTo returns expected boolean depending on conditions"() {
        given:
        def trueCondition = "trueCondition"
        IBaseResource fhirResource = new Bundle()

        def mockFhir = Mock(HapiFhirImplementation)
        mockFhir.evaluateCondition(_ as IBaseResource, trueCondition) >> true
        mockFhir.evaluateCondition(_ as IBaseResource, secondCondition) >> conditionResult
        TestApplicationContext.register(HapiFhir, mockFhir)
        TestApplicationContext.injectRegisteredImplementations()

        def rule = new ValidationRule(null, null, null, [
            trueCondition,
            secondCondition
        ], null)

        expect:
        rule.appliesTo(fhirResource) == applies

        where:
        secondCondition   | conditionResult | applies
        "secondCondition" | true            | true
        "secondCondition" | false           | false
    }

    def "appliesTo logs an error and returns false if an exception happens when evaluating a condition"() {
        given:
        def fhirResource = new Bundle()

        def mockFhir = Mock(HapiFhirImplementation)
        mockFhir.evaluateCondition(fhirResource, "condition") >> { throw new Exception() }
        TestApplicationContext.register(HapiFhir, mockFhir)
        TestApplicationContext.injectRegisteredImplementations()

        def rule = new ValidationRule(null, null, null, ["condition"], null)

        when:
        def applies = rule.appliesTo(fhirResource)

        then:
        1 * mockLogger.logError(_ as String, _ as Exception)
        !applies
    }

    def "isValid returns expected boolean depending on validations"() {
        given:
        def trueValidation = "trueValidation"
        def fhirResource = new Bundle()

        def mockFhir = Mock(HapiFhirImplementation)
        mockFhir.evaluateCondition(fhirResource, trueValidation) >> true
        mockFhir.evaluateCondition(fhirResource, secondValidation) >> validationResult
        TestApplicationContext.register(HapiFhir, mockFhir)
        TestApplicationContext.injectRegisteredImplementations()

        def rule = new ValidationRule(null, null, null, null, [
            trueValidation,
            secondValidation
        ])

        expect:
        rule.isValid(fhirResource) == valid

        where:
        secondValidation   | validationResult | valid
        "secondValidation" | true             | true
        "secondValidation" | false            | false
    }

    def "isValid logs an error and returns false if an exception happens when evaluating a validation"() {
        given:
        def fhirResource = new Bundle()

        def mockFhir = Mock(HapiFhirImplementation)
        mockFhir.evaluateCondition(fhirResource, "condition") >> { throw new Exception() }
        TestApplicationContext.register(HapiFhir, mockFhir)
        TestApplicationContext.injectRegisteredImplementations()

        def rule = new ValidationRule(null, null, null, null, ["validation"])

        when:
        def valid = rule.isValid(fhirResource)

        then:
        1 * mockLogger.logError(_ as String, _ as Exception)
        !valid
    }
}
