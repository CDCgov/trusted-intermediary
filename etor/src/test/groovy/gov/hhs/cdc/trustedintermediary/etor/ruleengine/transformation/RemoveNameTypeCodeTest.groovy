package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation

import gov.hhs.cdc.trustedintermediary.ExamplesHelper
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom.removeNameTypeCode
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiHelper
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Patient
import spock.lang.Specification

class RemoveNameTypeCodeTest extends Specification {
    def transformClass

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.injectRegisteredImplementations()

        transformClass = new removeNameTypeCode()
    }

    def "remove PID.5-7 from Bundle"() {
        given:
        def fhirResource = ExamplesHelper.getExampleFhirResource("../CA/002_CA_ORU_R01_initial_translation.fhir")
        def bundle = fhirResource.getUnderlyingResource() as Bundle
        def patient = HapiHelper.resourceInBundle(bundle, Patient) as Patient
        def patientName = patient.getName()
        def pid5_7 = getPid5_7(bundle)

        expect:
        pid5_7 != null

        when:
        transformClass.transform(fhirResource, null)
        def removedPid5_7 = getPid5_7(bundle)

        then:
        removedPid5_7 == null
    }

    def getPid5_7(Bundle bundle) {
        def patient = HapiHelper.resourceInBundle(bundle, Patient) as Patient
        def patientName = patient.getName()
        def extension = patientName.get(0).getExtensionByUrl("https://reportstream.cdc.gov/fhir/StructureDefinition/xpn-human-name")
        if (extension.hasExtension("XPN.7"))
            return patientName.get(0).getExtensionByUrl("https://reportstream.cdc.gov/fhir/StructureDefinition/xpn-human-name").getExtensionByUrl("XPN.7").getValue()
        else
            return null
    }
}
