package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation

import gov.hhs.cdc.trustedintermediary.ExamplesHelper
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.RuleLoader
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.validation.ValidationRule
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
