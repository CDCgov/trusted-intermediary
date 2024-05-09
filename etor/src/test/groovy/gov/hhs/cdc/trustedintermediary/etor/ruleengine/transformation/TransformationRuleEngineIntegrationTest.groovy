package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation

import gov.hhs.cdc.trustedintermediary.etor.ruleengine.RuleEngineHelper
import gov.hhs.cdc.trustedintermediary.ExamplesHelper
import gov.hhs.cdc.trustedintermediary.FhirBundleHelper
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.RuleLoader
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiFhirImplementation
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiFhirResource
import gov.hhs.cdc.trustedintermediary.external.jackson.Jackson
import gov.hhs.cdc.trustedintermediary.wrappers.HapiFhir
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
        TestApplicationContext.register(HapiFhir, fhir)
        TestApplicationContext.register(TransformationRuleEngine, engine)
        TestApplicationContext.register(RuleLoader, RuleLoader.getInstance())
        TestApplicationContext.register(Logger, mockLogger)
        TestApplicationContext.register(MetricMetadata, Mock(MetricMetadata))

        TestApplicationContext.injectRegisteredImplementations()

        engine.ensureRulesLoaded()
    }

    def "transformation rules run without error"() {
        given:
        def bundle = new Bundle()

        when:
        engine.runRules(new HapiFhirResource(bundle))

        then:
        0 * mockLogger.logError(_ as String, _ as Exception)
        (1.._) * mockLogger.logInfo(_ as String, _ as String)
    }

    def "all transformations in the definitions file have existing custom methods"() {
        when:
        def transformationMethodNames = engine.rules.collect { rule -> rule.name }

        then:
        transformationMethodNames.each { transformationMethodName ->
            assert TransformationRule.getTransformationInstance(transformationMethodName) != null
        }
    }

    def "test rule transformation accuracy: addEtorProcessingTag"() {
        given:
        def ruleName = 'addEtorProcessingTag'
        // we could also use this file for testing the rule: e2e/orders/001_OML_O21_short.fhir
        def bundle = new Bundle()
        def rule = RuleEngineHelper.getRuleByName(engine.rules, ruleName)

        expect:
        FhirBundleHelper.resourceInBundle(bundle, MessageHeader) == null

        when:
        rule.runRule(new HapiFhirResource(bundle))
        def messageHeader = FhirBundleHelper.resourceInBundle(bundle, MessageHeader)

        then:
        0 * mockLogger.logError(_ as String, _ as Exception)
        messageHeader.meta.tag.last().code == 'ETOR'
    }

    def "test rule transformation accuracy: convertToOmlOrder"() {
        given:
        def ruleName = 'convertToOmlOrder'
        // we could also use this file for testing the rule: e2e/orders/003_2_ORM_O01_short_linked_to_002_ORU_R01_short.fhir
        def bundle = FhirBundleHelper.createMessageBundle(messageTypeCode: 'ORM_O01')
        def rule = RuleEngineHelper.getRuleByName(engine.rules, ruleName)

        expect:
        FhirBundleHelper.resourceInBundle(bundle, MessageHeader).event.code == 'O01'

        when:
        rule.runRule(new HapiFhirResource(bundle))
        def messageHeader = FhirBundleHelper.resourceInBundle(bundle, MessageHeader)

        then:
        0 * mockLogger.logError(_ as String, _ as Exception)
        messageHeader.event.code == 'O21'
    }

    def "test rule transformation accuracy: addContactSectionToPatientResource"() {
        given:
        def ruleName = 'addContactSectionToPatientResource'
        // we could also use this file for testing the rule: e2e/orders/003_2_ORM_O01_short_linked_to_002_ORU_R01_short.fhir
        def bundle = FhirBundleHelper.createMessageBundle(messageTypeCode: 'OML_O21')
        bundle.addEntry(new Bundle.BundleEntryComponent().setResource(new Patient()))
        def rule = RuleEngineHelper.getRuleByName(engine.rules, ruleName)

        expect:
        FhirBundleHelper.resourceInBundle(bundle, Patient).contact.isEmpty()

        when:
        rule.runRule(new HapiFhirResource(bundle))
        def patient = FhirBundleHelper.resourceInBundle(bundle, Patient)

        then:
        0 * mockLogger.logError(_ as String, _ as Exception)
        patient.contact.size() > 0
    }

    def "consecutively applied transformations don't interfere with each other: 003_2_ORM_O01_short_linked_to_002_ORU_R01_short"() {
        given:
        def testFile = 'e2e/orders/003_2_ORM_O01_short_linked_to_002_ORU_R01_short.fhir'
        def transformationsToApply = [
            'convertToOmlOrder',
            'addContactSectionToPatientResource'
        ]
        def fhirResource = ExamplesHelper.getExampleFhirResource(testFile)
        def bundle = (Bundle) fhirResource.getUnderlyingResource()

        expect:
        FhirBundleHelper.resourceInBundle(bundle, MessageHeader).event.code == 'O01'
        FhirBundleHelper.resourceInBundle(bundle, Patient).contact.isEmpty()

        when:
        transformationsToApply.each { ruleName ->
            def rule = RuleEngineHelper.getRuleByName(engine.rules, ruleName)
            rule.runRule(fhirResource)
        }
        def messageHeader = FhirBundleHelper.resourceInBundle(bundle, MessageHeader)
        def patient = FhirBundleHelper.resourceInBundle(bundle, Patient)

        then:
        0 * mockLogger.logError(_ as String, _ as Exception)
        messageHeader.event.code == 'O21'
        patient.contact.size() > 0
    }
}
