package gov.hhs.cdc.trustedintermediary.external.hapi

import ca.uhn.fhir.fhirpath.FhirPathExecutionException
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import org.hl7.fhir.instance.model.api.IBaseResource
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.DiagnosticReport
import org.hl7.fhir.r4.model.ServiceRequest
import spock.lang.Specification

class HapiFhirImplementationTest extends Specification {
    Bundle bundle
    DiagnosticReport diaReport
    ServiceRequest servRequest
    HapiFhirImplementation fhir = HapiFhirImplementation.getInstance()

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
    }

    def "evaluateCondition returns true on finding existing value"() {
        given:
        def path = "Bundle.id.exists()"

        when:
        def result = fhir.evaluateCondition(bundle as IBaseResource, path)

        then:
        result == true
    }

    def "evaluateCondition returns false on not finding non-existing value"() {
        given:
        def path = "Bundle.timestamp.exists()"

        when:
        def result = fhir.evaluateCondition(bundle as IBaseResource, path)

        then:
        result == false
    }

    def "evaluateCondition returns false on not finding matching extension"() {
        given:
        def path = "Bundle.entry[0].resource.extension('blah')"

        when:
        def result = fhir.evaluateCondition(bundle as IBaseResource, path)

        then:
        result == false
    }

    def "evaluateCondition throws FhirPathExecutionException on empty string"() {
        given:
        def path = ""

        when:
        fhir.evaluateCondition(bundle as IBaseResource, path)

        then:
        thrown(FhirPathExecutionException)
    }

    def "evaluateCondition throws FhirPathExecutionException on fake method"() {
        given:
        def path = "Bundle.entry[0].resource.BadMethod('blah')"

        when:
        fhir.evaluateCondition(bundle as IBaseResource, path)

        then:
        thrown(FhirPathExecutionException)
    }
}
