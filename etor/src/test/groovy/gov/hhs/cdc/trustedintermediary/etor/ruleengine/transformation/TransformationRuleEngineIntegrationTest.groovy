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
import org.hl7.fhir.r4.model.Patient
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
            assert TransformationRule.loadClassFromCache(transformationMethodName) != null
        }
    }

    def "transformation rules run for specific test files and all rules have corresponding test files"() {
        given:
        def fhirResource = ExamplesHelper.getExampleFhirResource(testFile)
        0 * mockLogger.logError(_ as String, _ as Exception)
        1 * mockLogger.logInfo(_ as String, _ as String)

        expect:
        engine.getRuleByName(ruleName).runRule(fhirResource)

        where:
        ruleName                             | testFile
        "addEtorProcessingTag"               | "e2e/orders/001_OML_O21_short.fhir"
        "convertToOmlOrder"                  | "e2e/orders/003_2_ORM_O01_short_linked_to_002_ORU_R01_short.fhir"
        "addContactSectionToPatientResource" | "e2e/orders/003_2_ORM_O01_short_linked_to_002_ORU_R01_short.fhir"
        "convertDemographicsToOrder"         | "e2e/demographics/001_Patient_NBS.fhir"
    }

    def "Testing accuracy of rule: convertDemographicsToOrder"() {
        given:
        def untouchedBundle = new Bundle()
        def bundle = untouchedBundle.copy()
        engine.ensureRulesLoaded()
        engine.rules.removeAll(engine.rules.findAll {
            it.name != "convertDemographicsToOrder"
        })

        when:
        engine.runRules(new HapiFhirResource(bundle))

        then:
        untouchedBundle.entry.isEmpty()
        bundle.entry.size() == 3
        bundle.entry[0].getResource().fhirType() == "MessageHeader"
        bundle.entry[1].getResource().fhirType() == "ServiceRequest"
        bundle.entry[2].getResource().fhirType() == "Provenance"
    }

    def "Testing accuracy of rule: addEtorProcessingTag"() {
        given:
        def untouchedBundle = new Bundle()
        def bundle = untouchedBundle.copy()
        engine.ensureRulesLoaded()
        engine.rules.removeAll(engine.rules.findAll {
            it.name != "addEtorProcessingTag"
        })

        when:
        engine.runRules(new HapiFhirResource(bundle))

        then:
        untouchedBundle.entry.isEmpty()
        bundle.entry[0].getResource().meta.tag.last().code == "ETOR"
    }

    def "Testing accuracy of rule: convertToOmlOrder"() {
        given:
        def untouchedBundle = FhirBundleHelper.createMessageBundle(messageTypeCode: "ORM_O01")
        def bundle = untouchedBundle.copy()
        engine.ensureRulesLoaded()
        engine.rules.removeAll(engine.rules.findAll {
            it.name != "convertToOmlOrder"
        })

        when:
        engine.runRules(new HapiFhirResource(bundle))

        then:
        untouchedBundle.entry[0].getResource().event.code == "O01"
        bundle.entry[0].getResource().event.code == "O21"
    }

    def "Testing accuracy of rule: addContactSectionToPatientResource"() {
        given:
        def untouchedBundle = FhirBundleHelper.createMessageBundle(messageTypeCode: "OML_O21")
        untouchedBundle.addEntry(new Bundle.BundleEntryComponent().setResource(new Patient()))
        def bundle = untouchedBundle.copy()
        engine.ensureRulesLoaded()
        engine.rules.removeAll(engine.rules.findAll {
            it.name != "addContactSectionToPatientResource"
        })

        when:
        engine.runRules(new HapiFhirResource(bundle))

        then:
        def untouchedPatient = untouchedBundle.entry[2].getResource() as Patient
        untouchedPatient.contact.isEmpty()
        def patient = bundle.entry[2].getResource() as Patient
        patient.contact.size() > 0
    }
}
