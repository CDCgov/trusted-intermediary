package gov.hhs.cdc.trustedintermediary.external.hapi

import ca.uhn.fhir.model.primitive.IdDt
import org.hl7.fhir.r4.model.Organization
import org.hl7.fhir.r4.model.Reference
import spock.lang.Specification

class HapiFhirCustomEvaluationContextTest extends Specification {
    def context = new HapiFhirCustomEvaluationContext()

    def "resolveReference returns null if the context is NOT provided"() {
        when:
        def result = context.resolveReference(new IdDt(), null)

        then:
        result == null
    }

    def "resolveReference attempts to get a resource if the context is provided and is a reference"() {
        given:
        def refId = new IdDt()
        def org = new Organization()
        def theContext = new Reference(org)

        when:
        def result = context.resolveReference(refId, theContext)

        then:
        result == org
    }

    def "resolveReference returns the given context if it's NOT a reference"() {
        given:
        def refId = new IdDt()
        def org = new Organization()

        when:
        def result = context.resolveReference(refId, org)

        then:
        result == org
    }
}
