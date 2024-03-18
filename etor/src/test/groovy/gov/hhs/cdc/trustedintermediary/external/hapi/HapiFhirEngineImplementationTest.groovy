package gov.hhs.cdc.trustedintermediary.external.hapi

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.wrappers.HapiFhirEngine
import org.hl7.fhir.instance.model.api.IBaseResource
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.DiagnosticReport
import org.hl7.fhir.r4.model.ServiceRequest
import org.hl7.fhir.r4.utils.FHIRLexer
import spock.lang.Specification

// @todo build actual tests this is a skeleton
class HapiFhirEngineImplementationTest extends Specification {
    HapiFhirEngine engine
    Bundle bundle
    DiagnosticReport diaReport
    ServiceRequest servRequest

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.injectRegisteredImplementations()

        engine = new HapiFhirEngineImplementation()

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

    def cleanup() {
    }

    def "parsePath returns null on blank"() {
        given:
        def path = ""

        when:
        def result = engine.parsePath(path)

        then:
        result == null
    }

    def "parsePath returns not null on a valid"() {
        given:
        def path = "Bundle.entry.resource.ofType(MessageHeader)"

        when:
        def result = engine.parsePath(path)

        then:
        result != null
    }

    def "parsePath returns not null on a valid"() {
        given:
        def path = "%resource.contact.relationship.first().coding.exists()"

        when:
        def result = engine.parsePath(path)

        then:
        result != null
    }

    def "parsePath throws FHIRLexerException on fake method"() {
        given:
        def path = "Bundle.entry.resource.BadMethod(MessageHeader)"

        when:
        engine.parsePath(path)

        then:
        thrown(FHIRLexer.FHIRLexerException)
    }

    def "parsePath throws FHIRLexerException on bad syntax"() {
        given:
        def path = "Bundle...entry.resource.ofType(MessageHeader)"

        when:
        engine.parsePath(path)

        then:
        thrown(FHIRLexer.FHIRLexerException)
    }

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

    def "evaluateCondition returns false on empty string"() {
        given:
        def path = ""

        when:
        def result = engine.evaluateCondition(bundle as IBaseResource, path)

        then:
        result == false
    }

    def "evaluateCondition throws FHIRLexerException on fake method"() {
        given:
        def path = "Bundle.entry[0].resource.BadMethod('blah')"

        when:
        engine.evaluateCondition(bundle as IBaseResource, path)

        then:
        thrown(FHIRLexer.FHIRLexerException)
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

}
