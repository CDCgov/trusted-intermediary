package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation


import gov.hhs.cdc.trustedintermediary.HealthDataMock
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.validation.ValidationRule
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiFhirHelper
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiHelper
import gov.hhs.cdc.trustedintermediary.wrappers.HealthData
import gov.hhs.cdc.trustedintermediary.wrappers.HealthDataExpressionEvaluator
import gov.hhs.cdc.trustedintermediary.wrappers.HapiFhir
import gov.hhs.cdc.trustedintermediary.wrappers.Logger
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiFhirImplementation
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.MessageHeader
import spock.lang.Specification

class TransformationRuleTest extends Specification {

    def mockLogger = Mock(Logger)
    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(Logger, mockLogger)
        TestApplicationContext.register(HealthDataExpressionEvaluator, HapiFhirImplementation.getInstance())
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

    def "runRule() performs a transformation rule when one exists"() {
        given:
        def ruleName = "Rule name"
        def ruleDescription = "Rule Description"
        def ruleMessage = "Rule Warning Message"
        def ruleConditions = ["condition1", "condition2"]
        def ruleActions = [
            new TransformationRuleMethod("HappyPathCustomTransformationMockClass", null)
        ]
        TestApplicationContext.register(HapiFhir, Mock(HapiFhir))

        def rule = new TransformationRule(ruleName, ruleDescription, ruleMessage, ruleConditions, ruleActions)
        def fhirResource = new HealthDataMock(HapiFhirHelper.createMessageBundle(new HashMap()))

        when:
        rule.runRule(fhirResource)

        then:
        def messageHeader = HapiHelper.resourceInBundle(fhirResource.getUnderlyingData() as Bundle, MessageHeader.class) as MessageHeader
        messageHeader.getEventCoding().getCode() == "mock_code"
    }

    def "runRule logs an error and continues when an invalid class is given as input"() {
        given:
        def ruleName = "Rule name"
        def ruleDescription = "Rule Description"
        def ruleMessage = "Rule Warning Message"
        def ruleConditions = ["condition1", "condition2"]
        def ruleActions = [
            new TransformationRuleMethod("DoesNotCompute", null)
        ]
        TestApplicationContext.register(HapiFhir, Mock(HapiFhir))

        when:
        def rule = new TransformationRule(ruleName, ruleDescription, ruleMessage, ruleConditions, ruleActions)
        def fhirResource = new HealthDataMock(HapiFhirHelper.createMessageBundle(new HashMap()))
        rule.runRule(fhirResource)

        then:
        1 * mockLogger.logError(_, _)
    }

    def "runRule logs an error and continues when given a class without transform"() {
        given:
        def ruleName = "Rule name"
        def ruleDescription = "Rule Description"
        def ruleMessage = "Rule Warning Message"
        def ruleConditions = ["condition1", "condition2"]
        def ruleActions = [
            new TransformationRuleMethod("NoSuchMethodExceptionMockClass", null)
        ]

        TestApplicationContext.register(HapiFhir, Mock(HapiFhir))

        when:
        def rule = new TransformationRule(ruleName, ruleDescription, ruleMessage, ruleConditions, ruleActions)
        def fhirResource = new HealthDataMock(HapiFhirHelper.createMessageBundle(new HashMap()))
        rule.runRule(fhirResource)

        then:
        1 * mockLogger.logError(_, _)
    }

    def "runRule logs an error and continues when given abstract class input"() {
        given:
        def ruleName = "Rule name"
        def ruleDescription = "Rule Description"
        def ruleMessage = "Rule Warning Message"
        def ruleConditions = ["condition1", "condition2"]
        def ruleActions = [
            new TransformationRuleMethod("InstantiationExceptionMockClass", null)
        ]

        TestApplicationContext.register(HapiFhir, Mock(HapiFhir))

        when:
        def rule = new TransformationRule(ruleName, ruleDescription, ruleMessage, ruleConditions, ruleActions)
        def fhirResource = new HealthDataMock(HapiFhirHelper.createMessageBundle(new HashMap()))
        rule.runRule(fhirResource)

        then:
        1 * mockLogger.logError(_, _)
    }

    def "runRule logs an error and continues when given a private constructor class input"() {
        given:
        def ruleName = "Rule name"
        def ruleDescription = "Rule Description"
        def ruleMessage = "Rule Warning Message"
        def ruleConditions = ["condition1", "condition2"]
        def ruleActions = [
            new TransformationRuleMethod("IllegalAccessExceptionMockClass", null)
        ]

        TestApplicationContext.register(HapiFhir, Mock(HapiFhir))

        when:
        def rule = new TransformationRule(ruleName, ruleDescription, ruleMessage, ruleConditions, ruleActions)
        def fhirResource = new HealthDataMock(HapiFhirHelper.createMessageBundle(new HashMap()))
        rule.runRule(fhirResource)

        then:
        1 * mockLogger.logError(_, _)
    }

    def "runRule logs an error if passing more than one HealthData"() {
        given:
        def mockFhir = Mock(HapiFhirImplementation)
        TestApplicationContext.register(HealthDataExpressionEvaluator, mockFhir)

        def rule = new TransformationRule(null, null, null, ["condition"], [
            new TransformationRuleMethod("InstantiationExceptionMockClass", null)
        ])

        when:
        rule.runRule(Mock(HealthData), Mock(HealthData))

        then:
        1 * mockLogger.logError(_ as String)
    }
}
