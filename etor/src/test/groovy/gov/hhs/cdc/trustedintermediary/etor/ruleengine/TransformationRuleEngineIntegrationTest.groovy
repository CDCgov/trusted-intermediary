package gov.hhs.cdc.trustedintermediary.etor.ruleengine

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.TransformationRuleEngine
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.validation.ValidationRuleEngine
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiFhirImplementation
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiFhirResource
import gov.hhs.cdc.trustedintermediary.external.jackson.Jackson
import gov.hhs.cdc.trustedintermediary.wrappers.HapiFhir
import gov.hhs.cdc.trustedintermediary.wrappers.Logger
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.Formatter
import org.hl7.fhir.r4.model.Bundle
import spock.lang.Specification

class TransformationRuleEngineIntegrationTest  extends Specification {
    def fhir = HapiFhirImplementation.getInstance()
    def engine = TransformationRuleEngine.getInstance("transformation_definitions.json")
    def mockLogger = Mock(Logger)

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()

        TestApplicationContext.register(Formatter, Jackson.getInstance())
        TestApplicationContext.register(HapiFhir, fhir)
        TestApplicationContext.register(TransformationRuleEngine, engine)
        TestApplicationContext.register(RuleLoader, RuleLoader.getInstance())
        TestApplicationContext.register(Logger, mockLogger)

        TestApplicationContext.injectRegisteredImplementations()
    }

    def "transformation rules run without error"() {
        given:
        def bundle = new Bundle()

        when:
        engine.runRules(new HapiFhirResource(bundle))

        then:
        0 * mockLogger.logError(_ as String, _ as Exception)
    }

    def "transformation rules filter and run rules for Orders"() {
    }

    def "transformation rules filter and run rules for Results"() {
    }

    def "transformation rules filter and run rules for Demographics"() {
    }
}
