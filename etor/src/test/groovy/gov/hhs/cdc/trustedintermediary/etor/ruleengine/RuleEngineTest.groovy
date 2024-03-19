package gov.hhs.cdc.trustedintermediary.etor.ruleengine

import ca.uhn.fhir.fhirpath.FhirPathExecutionException
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiFhirImplementation
import gov.hhs.cdc.trustedintermediary.external.jackson.Jackson
import gov.hhs.cdc.trustedintermediary.wrappers.HapiFhir
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.Formatter
import org.hl7.fhir.instance.model.api.IBaseResource
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.DiagnosticReport
import org.hl7.fhir.r4.model.ServiceRequest
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path

class RuleEngineTest extends Specification {
    //    Bundle bundle
    //    DiagnosticReport diaReport
    //    ServiceRequest servRequest
    HapiFhir fhir = HapiFhirImplementation.getInstance()
    RuleEngine engine = RuleEngine.getInstance()

    String labOrderJsonFileString

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()

        TestApplicationContext.register(Formatter, Jackson.getInstance())
        TestApplicationContext.register(HapiFhirImplementation, HapiFhirImplementation.getInstance())
        TestApplicationContext.register(RuleEngine, RuleEngine.getInstance())
        TestApplicationContext.register(RuleLoader, RuleLoader.getInstance())

        TestApplicationContext.injectRegisteredImplementations()

        engine.loadRules()
    }

    def "first test"() {
        given:
        labOrderJsonFileString = Files.readString(Path.of("../examples/Test/Orders/003_AL_ORM_O01_NBS_Fully_Populated_1_hl7_translation.fhir"))
        def bundle = fhir.parseResource(labOrderJsonFileString, Bundle)

        when:
        engine.validate(bundle)

        then:
        1 == 1
    }
}
