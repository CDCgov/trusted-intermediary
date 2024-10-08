package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation


import gov.hhs.cdc.trustedintermediary.etor.ruleengine.TransformationRuleEngineHelper
import gov.hhs.cdc.trustedintermediary.ExamplesHelper
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.ruleengine.RuleLoader
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiFhirHelper
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiFhirImplementation
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiFhirResource
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiHelper
import gov.hhs.cdc.trustedintermediary.external.jackson.Jackson
import gov.hhs.cdc.trustedintermediary.wrappers.HealthDataExpressionEvaluator
import gov.hhs.cdc.trustedintermediary.wrappers.Logger
import gov.hhs.cdc.trustedintermediary.wrappers.MetricMetadata
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.Formatter
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.MessageHeader
import org.hl7.fhir.r4.model.Patient
import spock.lang.Specification

class TransformationRuleEngineIntegrationTest extends Specification {

    def engine = TransformationRuleEngine.getInstance('transformation_definitions.json')
    def fhir = HapiFhirImplementation.getInstance()
    def mockLogger = Mock(Logger)

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()

        TestApplicationContext.register(Formatter, Jackson.getInstance())
        TestApplicationContext.register(HealthDataExpressionEvaluator, fhir)
        TestApplicationContext.register(TransformationRuleEngine, engine)
        TestApplicationContext.register(RuleLoader, RuleLoader.getInstance())
        TestApplicationContext.register(Logger, mockLogger)
        TestApplicationContext.register(MetricMetadata, Mock(MetricMetadata))

        TestApplicationContext.injectRegisteredImplementations()

        engine.ensureRulesLoaded()
    }

    def "transformation rules run without error"() {
        given:
        def bundle = HapiFhirHelper.createMessageBundle(messageTypeCode: 'ORM_O01')

        when:
        engine.runRules(new HapiFhirResource(bundle))

        then:
        0 * mockLogger.logError(_ as String, _ as Exception)
        (1.._) * mockLogger.logInfo(_ as String)
    }

    def "all transformations in the definitions file have existing custom methods"() {
        when:
        def transformationMethodNames = engine.rules*.rules*.name.flatten()

        then:
        transformationMethodNames.each { transformationMethodName ->
            assert TransformationRule.getTransformationInstance(transformationMethodName) != null
        }
    }

    def "consecutively applied transformations don't interfere with each other: 003_2_ORM_O01_short_linked_to_002_ORU_R01_short"() {
        given:
        def testFile = 'e2e/orders/003_2_ORM_O01_short_linked_to_002_ORU_R01_short.fhir'
        def transformationsToApply = [
            'convertToOmlOrder',
            'addContactSectionToPatientResource'
        ]
        def fhirResource = ExamplesHelper.getExampleFhirResource(testFile)
        def bundle = (Bundle) fhirResource.getUnderlyingData()

        expect:
        HapiHelper.resourceInBundle(bundle, MessageHeader).event.code == 'O01'
        HapiHelper.resourceInBundle(bundle, Patient).contact.isEmpty()

        when:
        transformationsToApply.each { ruleName ->
            def rule = TransformationRuleEngineHelper.getRuleByName(engine.rules, ruleName)
            rule.runRule(fhirResource)
        }
        def messageHeader = HapiHelper.resourceInBundle(bundle, MessageHeader) as MessageHeader
        def patient = HapiHelper.resourceInBundle(bundle, Patient) as Patient

        then:
        0 * mockLogger.logError(_ as String, _ as Exception)
        messageHeader.event.code == 'O21'
        patient.contact.size() > 0
    }
}
