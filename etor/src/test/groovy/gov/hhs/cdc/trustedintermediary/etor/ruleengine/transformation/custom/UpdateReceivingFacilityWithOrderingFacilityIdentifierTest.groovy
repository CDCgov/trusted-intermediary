package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom

import gov.hhs.cdc.trustedintermediary.ExamplesHelper
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.RuleExecutionException
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiFhirResource
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiHelper
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Organization
import spock.lang.Specification

class UpdateReceivingFacilityWithOrderingFacilityIdentifierTest extends Specification {

    def transformClass

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.injectRegisteredImplementations()

        transformClass = new updateReceivingFacilityWithOrderingFacilityIdentifier()
    }

    def "update california receiving facility with ordering facility identifier"() {
        given:
        def fhirResource = ExamplesHelper.getExampleFhirResource('../MN/004_MN_ORU_R01_NBS_1_hl7_translation.fhir')
        def bundle = fhirResource.getUnderlyingResource() as Bundle
        def diagnosticReport = HapiHelper.getDiagnosticReport(bundle)
        def serviceRequest = HapiHelper.getServiceRequestBasedOn(diagnosticReport)
        def practitionerRole = HapiHelper.getPractitionerRoleRequester(serviceRequest)
        def organization = HapiHelper.getOrganization(practitionerRole)
        def orcTwentyOneDotTen = HapiHelper.getOrc21Extension(organization).getValue() as String

        expect:
        HapiHelper.getReceivingFacility(bundle).getIdentifier().size() > 1
        HapiHelper.getSendingFacilityNamespace(bundle).getValue() != orcTwentyOneDotTen

        when:
        transformClass.transform(new HapiFhirResource(bundle), null)

        then:
        HapiHelper.getSendingFacilityNamespace(bundle).getValue() == orcTwentyOneDotTen
    }

    def "throw RuleExecutionException if receiving facility not in bundle"() {
        given:
        def bundle = new Bundle()
        HapiHelper.createMessageHeader(bundle)

        when:
        transformClass.transform(new HapiFhirResource(bundle), null)

        then:
        thrown(RuleExecutionException)
    }
}
