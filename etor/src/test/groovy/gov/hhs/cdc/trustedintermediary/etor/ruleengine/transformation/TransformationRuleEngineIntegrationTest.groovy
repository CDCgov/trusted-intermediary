package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation


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
        def transformationMethodNames = engine.rules.collectMany {
            it.rules.collect {
                it.name()
            }
        }

        then:
        transformationMethodNames.each { transformationMethodName ->
            assert TransformationRule.loadClassFromCache(transformationMethodName) != null
        }
    }

    def "transformation rules run for specific test files and all rules have corresponding test files"() {
        given:
        Map<String, String> transformationSampleMap = Map.of(
                "addEtorProcessingTag", "e2e/orders/001_OML_O21_short.fhir",
                "convertDemographicsToOrder", "e2e/demographics/001_Patient_NBS.fhir",
                "convertToOmlOrder", "e2e/orders/003_2_ORM_O01_short_linked_to_002_ORU_R01_short.fhir",
                "addContactSectionToPatientResource", "e2e/orders/003_2_ORM_O01_short_linked_to_002_ORU_R01_short.fhir",
                )

        engine.rules.each { rule ->
            when:
            def testFile = transformationSampleMap.get(rule.name)
            def fhirResource = ExamplesHelper.getExampleFhirResource(testFile)
            rule.runRule(fhirResource)

            then:
            0 * mockLogger.logError(_ as String, _ as Exception)
            1 * mockLogger.logInfo(_ as String, _ as String)
        }
    }
}
