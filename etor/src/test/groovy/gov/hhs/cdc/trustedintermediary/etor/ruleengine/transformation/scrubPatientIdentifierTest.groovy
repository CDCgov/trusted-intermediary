package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation

import gov.hhs.cdc.trustedintermediary.ExamplesHelper
import gov.hhs.cdc.trustedintermediary.FhirBundleHelper
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom.removeMessageTypeStructure
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom.scrubPatientIdentifiers
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiFhirResource
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiHelper
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Patient
import spock.lang.Specification

class scrubPatientIdentifierTest extends Specification {
    def transformClass

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.injectRegisteredImplementations()

        transformClass = new scrubPatientIdentifiers()
    }

    def "remove PID stuff"() {
        given:
        def fhirResource = ExamplesHelper.getExampleFhirResource("../MN/004_MN_ORU_R01_NBS_1_hl7_translation.fhir")

        expect:
        def bundle = fhirResource.getUnderlyingResource() as Bundle
        def patient = (Patient) HapiHelper.resourceInBundle(bundle, Patient) as Patient
        def identifier = patient.getIdentifier().get(0).getAssigner().getIdentifier().getValue()
        // assert bundle has PID.3-4 and PID.3-5

        when:
        transformClass.transform(fhirResource, null)

        then:
        true
        // assert bundle doesn't have PID.3-4 and PID.3-5
    }
}
