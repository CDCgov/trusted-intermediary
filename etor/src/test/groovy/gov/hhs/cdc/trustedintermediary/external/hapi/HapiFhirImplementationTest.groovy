package gov.hhs.cdc.trustedintermediary.external.hapi

import ca.uhn.fhir.fhirpath.FhirPathExecutionException
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import org.hl7.fhir.instance.model.api.IBaseResource
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.DiagnosticReport
import org.hl7.fhir.r4.model.ServiceRequest
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path

class HapiFhirImplementationTest extends Specification {
    Bundle bundle
    DiagnosticReport diaReport
    ServiceRequest servRequest
    HapiFhirImplementation engine = HapiFhirImplementation.getInstance()

    String labOrderJsonFileString

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(HapiFhirImplementation, HapiFhirImplementation.getInstance())

        TestApplicationContext.injectRegisteredImplementations()

        bundle = new Bundle()
        bundle.id = "abc123"

        diaReport = new DiagnosticReport()
        diaReport.id = "ghi789"
        servRequest = new ServiceRequest()
        servRequest.id = "def456"

        def entry1 = new Bundle.BundleEntryComponent()
        entry1.resource = diaReport
        bundle.addEntry(entry1)

        def entry2 = new Bundle.BundleEntryComponent()
        entry2.resource = servRequest
        bundle.addEntry(entry2)

        labOrderJsonFileString = Files.readString(Path.of("/Users/turke/Documents/GitHub/trusted-intermediary/examples/Test/Orders/003_AL_ORM_O01_NBS_Fully_Populated_1_hl7_translation.fhir"))
    }

    def "evaluate returns the correct resource if it's available"() {
        given:
        def path = "Bundle.entry.resource.ofType(DiagnosticReport)[0]"

        when:
        def result = engine.evaluate(bundle as IBaseResource, path)

        then:
        result.size() == 1
        result.first().class == DiagnosticReport.class
        (result.first() as DiagnosticReport).id == diaReport.id
    }

    def "evaluate returns empty list on bad extension names"() {
        given:
        def path = "Bundle.extension('blah').value"

        when:
        def result = engine.evaluate(bundle as IBaseResource, path)

        then:
        result.isEmpty()
    }

    def "evaluate throws FhirPathExecutionException on empty path"() {
        given:
        def path = ""

        when:
        engine.evaluate(bundle as IBaseResource, path)

        then:
        thrown(FhirPathExecutionException)
    }

    def "evaluate returns true on successful boolean checks"() {
        given:
        def path = "Bundle.entry.resource.ofType(DiagnosticReport)[0].exists()"

        when:
        def result = engine.evaluate(bundle as IBaseResource, path)

        then:
        result[0].primitiveValue() == "true"
    }

    def "evaluate returns false on unsuccessful boolean checks"() {
        given:
        def path = "Bundle.entry.resource.ofType(Practitioner)[0].exists()"

        when:
        def result = engine.evaluate(bundle as IBaseResource, path)

        then:
        result[0].primitiveValue() == "false"
    }

    def "evaluate throws FhirPathExecutionException on invalid boolean checks"() {
        given:
        def path = "Bundle.entry.resource.ofType(SomeFakeType)[0].exists()"

        when:
        engine.evaluate(bundle as IBaseResource, path)

        then:
        thrown(FhirPathExecutionException)
    }
    //    Bundle.entry.resource.ofType(MessageHeader).destination.receiver.resolve().identifier.value.exists()

    def "evaluateCondition returns true on finding existing value"() {
        given:
        def path = "Bundle.id.exists()"

        when:
        def result = engine.evaluateCondition(bundle as IBaseResource, path)

        then:
        result == true
    }

    def "evaluateCondition returns false on not finding non-existing value"() {
        given:
        def path = "Bundle.timestamp.exists()"

        when:
        def result = engine.evaluateCondition(bundle as IBaseResource, path)

        then:
        result == false
    }

    def "evaluateCondition returns false on not finding matching extension"() {
        given:
        def path = "Bundle.entry[0].resource.extension('blah')"

        when:
        def result = engine.evaluateCondition(bundle as IBaseResource, path)

        then:
        result == false
    }

    def "evaluateCondition throws FhirPathExecutionException on empty string"() {
        given:
        def path = ""

        when:
        engine.evaluateCondition(bundle as IBaseResource, path)

        then:
        thrown(FhirPathExecutionException)
    }

    def "evaluateCondition throws FhirPathExecutionException on fake method"() {
        given:
        def path = "Bundle.entry[0].resource.BadMethod('blah')"

        when:
        engine.evaluateCondition(bundle as IBaseResource, path)

        then:
        thrown(FhirPathExecutionException)
    }
}
