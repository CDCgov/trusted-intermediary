package gov.hhs.cdc.trustedintermediary.etor.ruleengine

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiFhirImplementation
import org.hl7.fhir.r4.model.Bundle
import spock.lang.Specification

class ValidationRuleTest extends Specification {

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
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
        def fhirResource = new Bundle()
        def validationRule = new ValidationRule(null, null, null, [
            trueCondition,
            secondCondition
        ], null)

        def mockFhir = Mock(HapiFhirImplementation)
        mockFhir.evaluateCondition(fhirResource, trueCondition) >> true
        mockFhir.evaluateCondition(fhirResource, secondCondition) >> conditionResult
        validationRule.fhirEngine = mockFhir

        expect:
        validationRule.appliesTo(fhirResource) == applies

        where:
        secondCondition   | conditionResult | applies
        "secondCondition" | true            | true
        "secondCondition" | false           | false
    }

    def "isValid returns expected boolean depending on validations"() {
        given:
        def trueValidation = "trueValidation"
        def fhirResource = new Bundle()
        def validationRule = new ValidationRule(null, null, null, null, [
            trueValidation,
            secondValidation
        ])

        def mockFhir = Mock(HapiFhirImplementation)
        mockFhir.evaluateCondition(fhirResource, trueValidation) >> true
        mockFhir.evaluateCondition(fhirResource, secondValidation) >> validationResult
        validationRule.fhirEngine = mockFhir

        expect:
        validationRule.isValid(fhirResource) == valid

        where:
        secondValidation   | validationResult | valid
        "secondValidation" | true             | true
        "secondValidation" | false            | false
    }
}
