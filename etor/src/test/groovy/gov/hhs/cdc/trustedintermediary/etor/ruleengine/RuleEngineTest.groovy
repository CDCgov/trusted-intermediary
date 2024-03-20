package gov.hhs.cdc.trustedintermediary.etor.ruleengine

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiFhirImplementation
import gov.hhs.cdc.trustedintermediary.external.jackson.Jackson
import gov.hhs.cdc.trustedintermediary.wrappers.Logger
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.Formatter
import org.hl7.fhir.r4.model.Bundle
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path

class RuleEngineTest extends Specification {
    def fhir = HapiFhirImplementation.getInstance()
    def engine = RuleEngine.getInstance()
    def mockLogger = Mock(Logger)

    String fhirBody

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()

        TestApplicationContext.register(Formatter, Jackson.getInstance())
        TestApplicationContext.register(HapiFhirImplementation, fhir)
        TestApplicationContext.register(RuleEngine, engine)
        TestApplicationContext.register(RuleLoader, RuleLoader.getInstance())
        TestApplicationContext.register(Logger, mockLogger)

        TestApplicationContext.injectRegisteredImplementations()
    }

    def "validation logs a warning when a validation fails"() {
        given:
        fhirBody = Files.readString(Path.of("../examples/Test/Orders/001_OML_O21_short.fhir"))
        def bundle = fhir.parseResource(fhirBody, Bundle)

        when:
        engine.validate(bundle)

        then:
        1 * mockLogger.logWarning(_ as String)
    }
}
