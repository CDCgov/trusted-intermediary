package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation

import gov.hhs.cdc.trustedintermediary.FhirBundleHelper
import gov.hhs.cdc.trustedintermediary.FhirResourceMock
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiTestHelper
import gov.hhs.cdc.trustedintermediary.wrappers.HapiFhir
import gov.hhs.cdc.trustedintermediary.wrappers.Logger
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.MessageHeader
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

    def "runRule() performs a transformation rule when one exists"() {
        given:
        def ruleName = "Rule name"
        def ruleDescription = "Rule Description"
        def ruleMessage = "Rule Warning Message"
        def ruleConditions = ["condition1", "condition2"]
        def ruleActions = [
            new TransformationRuleMethod("HappyPathMockClass", null)
        ]
        TestApplicationContext.register(HapiFhir, Mock(HapiFhir))

        when:
        def rule = new TransformationRule(ruleName, ruleDescription, ruleMessage, ruleConditions, ruleActions)
        def fhirResource = new FhirResourceMock(FhirBundleHelper.createMessageBundle(new HashMap()))
        rule.runRule(fhirResource)

        then:
        def messageHeaderStream = HapiTestHelper.resourceInBundle(fhirResource.getUnderlyingResource() as Bundle, MessageHeader.class)
        def actualMessageHeader = messageHeaderStream.filter{resource -> resource.getEventCoding().getCode().equals("mock_code")}.findFirst().orElse(null)
        actualMessageHeader.getEventCoding().getCode() == "mock_code"
    }

    def "runRule() throws RuntimeException when an invalid class is given as input"() {
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
        def fhirResource = new FhirResourceMock(FhirBundleHelper.createMessageBundle(new HashMap()))
        rule.runRule(fhirResource)

        then:
        thrown(RuntimeException)
    }

    def "runRule() throws NoSuchMethodException when given a class without transform"() {
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
        def fhirResource = new FhirResourceMock(FhirBundleHelper.createMessageBundle(new HashMap()))
        rule.runRule(fhirResource)

        then:
        1 * mockLogger.logError(_, _)
    }

    def "runRule() throws InstantiationException when given abstract class input"() {
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
        def fhirResource = new FhirResourceMock(FhirBundleHelper.createMessageBundle(new HashMap()))
        rule.runRule(fhirResource)

        then:
        1 * mockLogger.logError(_, _)
    }

    def "runRule() throws IllegalAccessException when given a private constructor class input"() {
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
        def fhirResource = new FhirResourceMock(FhirBundleHelper.createMessageBundle(new HashMap()))
        rule.runRule(fhirResource)

        then:
        1 * mockLogger.logError(_, _)
    }
}
