package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation

import gov.hhs.cdc.trustedintermediary.FhirBundleHelper
import gov.hhs.cdc.trustedintermediary.ExamplesHelper
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
import org.hl7.fhir.r4.model.ServiceRequest
import org.hl7.fhir.r4.model.Provenance
import spock.lang.Specification

class TransformationRuleEngineIntegrationTest extends Specification {
    def engine = TransformationRuleEngine.getInstance("transformation_definitions.json")
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
            assert TransformationRule.loadClassFromCache(transformationMethodName) != null
        }
    }

    def "test rule transformation accuracy: addEtorProcessingTag"() {
        given:
        def ruleName = "addEtorProcessingTag"
        // we could also use this file for testing the rule: e2e/orders/001_OML_O21_short.fhir
        def bundle = new Bundle()
        def untouchedBundle = bundle.copy()

        engine.ensureRulesLoaded()
        def rule = engine.getRuleByName(ruleName)

        when:
        rule.runRule(new HapiFhirResource(bundle))
        def messageHeader = FhirBundleHelper.resourceInBundle(bundle, MessageHeader)

        then:
        0 * mockLogger.logError(_ as String, _ as Exception)
        !bundle.equalsDeep(untouchedBundle)
        messageHeader.meta.tag.last().code == "ETOR"
    }

    def "test rule transformation accuracy: convertToOmlOrder"() {
        given:
        def ruleName = "convertToOmlOrder"
        // we could also use this file for testing the rule: e2e/orders/003_2_ORM_O01_short_linked_to_002_ORU_R01_short.fhir
        def bundle = FhirBundleHelper.createMessageBundle(messageTypeCode: "ORM_O01")
        def untouchedBundle = bundle.copy()

        engine.ensureRulesLoaded()
        def rule = engine.getRuleByName(ruleName)

        when:
        rule.runRule(new HapiFhirResource(bundle))
        def messageHeader = FhirBundleHelper.resourceInBundle(bundle, MessageHeader)
        def untouchedMessageHeader = FhirBundleHelper.resourceInBundle(untouchedBundle, MessageHeader)

        then:
        0 * mockLogger.logError(_ as String, _ as Exception)
        !bundle.equalsDeep(untouchedBundle)
        untouchedMessageHeader.event.code == "O01"
        messageHeader.event.code == "O21"
    }

    def "test rule transformation accuracy: addContactSectionToPatientResource"() {
        given:
        def ruleName = "addContactSectionToPatientResource"

        // we could also use this file for testing the rule: e2e/orders/003_2_ORM_O01_short_linked_to_002_ORU_R01_short.fhir
        def bundle = FhirBundleHelper.createMessageBundle(messageTypeCode: "OML_O21")
        bundle.addEntry(new Bundle.BundleEntryComponent().setResource(new Patient()))
        def untouchedBundle = bundle.copy()

        engine.ensureRulesLoaded()
        def rule = engine.getRuleByName(ruleName)

        when:
        rule.runRule(new HapiFhirResource(bundle))
        def patient = FhirBundleHelper.resourceInBundle(bundle, Patient.class)
        def untouchedPatient = FhirBundleHelper.resourceInBundle(untouchedBundle, Patient)

        then:
        0 * mockLogger.logError(_ as String, _ as Exception)
        !bundle.equalsDeep(untouchedBundle)
        untouchedPatient.contact.isEmpty()
        patient.contact.size() > 0
    }

    //    todo: ignoring while figuring out how to filter the demographics example
    //    def "test rule transformation accuracy: convertDemographicsToOrder"() {
    //        given:
    //        def ruleName = "convertDemographicsToOrder"
    //        def testFile = "e2e/demographics/001_Patient_NBS.fhir"
    //
    //        def fhirResource = ExamplesHelper.getExampleFhirResource(testFile)
    //        def bundle = (Bundle) fhirResource.getUnderlyingResource()
    //        def untouchedBundle = bundle.copy()
    //
    //        engine.ensureRulesLoaded()
    //        def rule = engine.getRuleByName(ruleName)
    //
    //        when:
    //        rule.runRule(fhirResource)
    //        def messageHeader = FhirBundleHelper.resourceInBundle(bundle, MessageHeader)
    //        def serviceRequest = FhirBundleHelper.resourceInBundle(bundle, ServiceRequest)
    //        def provenance = FhirBundleHelper.resourceInBundle(bundle, Provenance)
    //
    //        then:
    //        0 * mockLogger.logError(_ as String, _ as Exception)
    //        !bundle.equalsDeep(untouchedBundle)
    //        messageHeader != null
    //        serviceRequest != null
    //        provenance != null
    //    }
}
