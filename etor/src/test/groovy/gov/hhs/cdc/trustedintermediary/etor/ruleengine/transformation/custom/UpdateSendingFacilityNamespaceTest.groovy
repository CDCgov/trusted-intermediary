package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom

import gov.hhs.cdc.trustedintermediary.ExamplesHelper
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiFhirResource
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiHelper
import gov.hhs.cdc.trustedintermediary.wrappers.MetricMetadata
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Organization
import spock.lang.Specification

class UpdateSendingFacilityNamespaceTest extends Specification {

    def transformClass

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(MetricMetadata, Mock(MetricMetadata))
        TestApplicationContext.injectRegisteredImplementations()

        transformClass = new updateSendingFacilityNamespace()
    }

    def "update sending facility namespace to given name"() {
        given:
        def name = "CDPH"
        def fhirResource = ExamplesHelper.getExampleFhirResource("../MN/004_MN_ORU_R01_NBS_1_hl7_translation.fhir")
        def bundle = fhirResource.getUnderlyingResource() as Bundle

        expect:
        def existingOrg = HapiHelper.getMessageHeader(bundle).getSender().getResource() as Organization
        existingOrg.name != name

        when:
        transformClass.transform(new HapiFhirResource(bundle), Map.of("name", name))
        def header = HapiHelper.getMessageHeader(bundle)
        def org = header.getSender().getResource() as Organization

        then:
        org.name == name
    }
}
