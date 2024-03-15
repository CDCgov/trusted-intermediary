package gov.hhs.cdc.trustedintermediary.external.hapi

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.wrappers.HapiFhirEngine
import org.hl7.fhir.instance.model.api.IBaseResource
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.utils.FHIRLexer
import spock.lang.Specification

// @todo build actual tests this is a skeleton
class HapiFhirEngineImplementationTest extends Specification {
    HapiFhirEngine engine
    Bundle bundle

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.injectRegisteredImplementations()

        engine = new HapiFhirEngineImplementation()

        bundle = new Bundle()
        bundle.id = "abc123"
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
        def path = "Bundle.entry.resource.BADMETHOD(MessageHeader)"

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
}
