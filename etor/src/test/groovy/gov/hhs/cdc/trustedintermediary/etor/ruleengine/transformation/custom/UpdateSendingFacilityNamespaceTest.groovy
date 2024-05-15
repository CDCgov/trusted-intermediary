package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom

import gov.hhs.cdc.trustedintermediary.ExamplesHelper
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiFhirResource
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiHelper
import org.hl7.fhir.r4.model.Bundle
import spock.lang.Specification

class UpdateSendingFacilityNamespaceTest extends Specification {

    def transformClass

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.injectRegisteredImplementations()

        transformClass = new UpdateSendingFacilityNamespace()
    }

    def "update sending facility namespace to given name and remove other identifiers"() {
        given:
        def name = "CDPH"
        def fhirResource = ExamplesHelper.getExampleFhirResource("../MN/004_MN_ORU_R01_NBS_1_hl7_translation.fhir")
        def bundle = fhirResource.getUnderlyingResource() as Bundle

        expect:
        HapiHelper.getSendingFacility(bundle).getIdentifier().size() > 1
        HapiHelper.getSendingFacilityNamespace(bundle).getValue() != name

        when:
        transformClass.transform(new HapiFhirResource(bundle), Map.of("name", name))

        then:
        HapiHelper.getSendingFacility(bundle).getIdentifier().size() == 1
        HapiHelper.getSendingFacilityNamespace(bundle).getValue() == name
    }
}
