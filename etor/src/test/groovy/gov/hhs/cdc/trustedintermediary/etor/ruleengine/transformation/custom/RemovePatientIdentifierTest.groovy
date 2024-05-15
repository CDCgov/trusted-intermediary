package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom

import gov.hhs.cdc.trustedintermediary.ExamplesHelper
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiHelper
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Organization
import org.hl7.fhir.r4.model.Patient
import spock.lang.Specification

class RemovePatientIdentifierTest extends Specification {
    def transformClass

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.injectRegisteredImplementations()

        transformClass = new RemovePatientIdentifiers()
    }

    def "remove PID.3-4 and PID.3-5 from Bundle"() {
        given:
        def fhirResource = ExamplesHelper.getExampleFhirResource("../MN/004_MN_ORU_R01_NBS_1_hl7_translation.fhir")
        def bundle = fhirResource.getUnderlyingResource() as Bundle
        def result = getPid3_4AndPid3_5(bundle)
        def pid3_4 = result[0]
        def pid3_5 = result[1]

        expect:
        pid3_4 != null
        pid3_5 != null

        when:
        transformClass.transform(fhirResource, null)
        def strippedResult = getPid3_4AndPid3_5(bundle)
        def strippedPid3_4 = strippedResult[0]
        def strippedPid3_5 = strippedResult[1]

        then:
        strippedPid3_4 == null
        strippedPid3_5 == null
    }

    def getPid3_4AndPid3_5(Bundle bundle) {
        def patient = HapiHelper.resourceInBundle(bundle, Patient) as Patient
        def patientIdentifier = patient.getIdentifierFirstRep()
        def org = patientIdentifier.getAssigner().getResource() as Organization
        def pid3_4 = org.getIdentifierFirstRep().getValue()
        def pid3_5 = patientIdentifier.getType().getCodingFirstRep().getCode()
        return [pid3_4, pid3_5]
    }
}
