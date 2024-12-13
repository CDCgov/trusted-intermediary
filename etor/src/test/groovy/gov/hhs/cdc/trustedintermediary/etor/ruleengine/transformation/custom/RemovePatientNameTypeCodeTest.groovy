package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom

import gov.hhs.cdc.trustedintermediary.ExamplesHelper

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiFhirHelper
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiFhirResource
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiHelper
import org.hl7.fhir.r4.model.Bundle
import spock.lang.Specification

class RemovePatientNameTypeCodeTest extends Specification {
    def transformClass

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.injectRegisteredImplementations()

        transformClass = new RemovePatientNameTypeCode()
    }

    def "remove PID.5-7 from Bundle"() {
        given:
        def fhirResource = ExamplesHelper.getExampleFhirResource("../CA/007_CA_ORU_R01_CDPH_produced_UCSD2024-07-11-16-02-17-749_1_hl7_translation.fhir")
        def bundle = fhirResource.getUnderlyingData() as Bundle
        def pid5Extension = HapiHelper.getPID5Extension(bundle)
        def patientName = HapiHelper.getPIDPatient(bundle).getNameFirstRep()
        def patientNameUse = patientName.getUse()

        expect:
        pid5Extension.getExtensionByUrl(HapiHelper.EXTENSION_XPN7_URL) != null
        patientNameUse.toString() == "OFFICIAL"

        when:
        transformClass.transform(fhirResource, null)

        then:
        pid5Extension.getExtensionByUrl(HapiHelper.EXTENSION_XPN7_URL) == null
        !patientName.hasUse()
    }

    def "don't throw exception if patient resource not present"() {
        given:
        def bundle = new Bundle()
        HapiHelper.createMSHMessageHeader(bundle)

        when:
        transformClass.transform(new HapiFhirResource(bundle), null)

        then:
        noExceptionThrown()
    }
}
