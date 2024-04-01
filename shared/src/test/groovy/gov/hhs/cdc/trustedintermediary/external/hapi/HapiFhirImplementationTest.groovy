package gov.hhs.cdc.trustedintermediary.external.hapi

import ca.uhn.fhir.fhirpath.FhirPathExecutionException
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.wrappers.FhirParseException
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

    def "getStringFromFhirPath returns correct string value for existing path"() {
        given:
        def path = "Bundle.entry[0].resource.id"
        def expected = diaReport.id

        when:
        def actual = fhir.getStringFromFhirPath(bundle as IBaseResource, path)

        then:
        actual == expected
    }

    def "getStringFromFhirPath returns empty string fro non-existing path"() {
        given:
        def path = "Bundle.entry[0].resource.nonExistingProperty"
        def expected = ""

        when:
        def actual = fhir.getStringFromFhirPath(bundle as IBaseResource, path)

        then:
        actual == expected
    }

    def "parseResource can convert a valid string to Bundle"() {
        given:
        def fhirBody = Files.readString(Path.of("../examples/Test/Orders/001_OML_O21_short.fhir"))

        when:
        def parsedBundle = fhir.parseResource(fhirBody, Bundle.class)

        then:
        parsedBundle.class == Bundle.class
    }

    def "parseResource throws FhirParseException on an invalid string"() {
        when:
        fhir.parseResource("badString", Bundle.class)

        then:
        thrown(FhirParseException)
    }

    def "encodeResourceToJson successfully converts a Bundle to a string" () {
        when:
        def encodedBundle = fhir.encodeResourceToJson(bundle)

        then:
        encodedBundle.class == String.class
    }
}
