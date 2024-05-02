package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation

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
        bundle.entry[0].getResource().meta.tag[0].code == "ETOR"
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

    //    def "transformation rules filter and run rules for ORM messages"() {
    //        given:
    //        def fhirResource = ExamplesHelper.getExampleFhirResource(testFile)
    //        def rule = createTransformationRuleFromName(transformationMethodName)
    //        0 * mockLogger.logError(_ as String, _ as Exception)
    //
    //        when:
    //        def testFile = "e2e/orders/001_OML_O21_short.fhir"
    //        def transformationMethodName = "addContactSectionToPatientResource"
    //        def fhirResource = ExamplesHelper.getExampleFhirResource(testFile)
    //        def transformedFhirResource = addContactSectionToPatientResource(fhirResource)
    //
    //        then:
    //        def rule = createTransformationRuleFromName(transformationMethodName)
    //        rule.runRule(fhirResource)
    //
    //        then:
    //
    ////        expect:
    ////        rule.runRule(fhirResource)
    ////
    ////        where:
    ////        testFile                            | transformationMethodName
    ////        "e2e/orders/001_OML_O21_short.fhir" | "addContactSectionToPatientResource"
    //    }

    def "transformation rules filter and run rules for OML messages"() {
    }

    def "transformation rules filter and run rules for ORU messages"() {
    }

    def "transformation rules filter and run rules for Demographics"() {
    }

    TransformationRule createTransformationRule(List<String> conditions, List<TransformationRuleMethod> transformations) {
        return new TransformationRule(
                "Rule name",
                "Rule description",
                "Rule message",
                conditions,
                transformations,
                )
    }

    TransformationRule createTransformationRuleFromName(String transformationName) {
        return new TransformationRule(
                "Rule name",
                "Rule description",
                "Rule message",
                [],
                [
                    new TransformationRuleMethod(transformationName, new HashMap<String, String>())
                ]
                )
    }
}
