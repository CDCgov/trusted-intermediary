package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiFhirHelper
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiFhirResource
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiHelper
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Coding
import org.hl7.fhir.r4.model.Extension
import org.hl7.fhir.r4.model.Observation
import org.hl7.fhir.r4.model.StringType
import spock.lang.Specification

class MapLocalObservationCodesTest extends Specification {
    def transformClass

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.injectRegisteredImplementations()

        transformClass = new MapLocalObservationCodes()
    }

    def "When message has a observation code, should add the LOINC-mapped code to OBX-3.1/2/3"() {
        given:
        final String LOCAL_CODE = "99717-32"
        final String LOCAL_DISPLAY = "Adrenoleukodystrophy deficiency newborn screening interpretation"
        final String LOCAL_CODING = "alt-coding"
        final String LOCAL_CODING_SYSTEM = "alt-coding"

        def bundle = HapiFhirHelper.createMessageBundle(messageTypeCode: 'ORU_R01')

        def coding = new Coding()
        coding.system = HapiHelper.LOCALLY_DEFINED_CODE
        coding.code = LOCAL_CODE
        coding.display = LOCAL_DISPLAY

        coding.addExtension(HapiHelper.EXTENSION_CWE_CODING, new StringType(LOCAL_CODING))
        coding.addExtension(HapiHelper.EXTENSION_CODING_SYSTEM, new StringType(LOCAL_CODING_SYSTEM))

        def observation = new Observation()
        observation.code.addCoding(coding)
        bundle.addEntry(new Bundle.BundleEntryComponent().setResource(observation))

        when:
        transformClass.transform(new HapiFhirResource(bundle), null)

        then:
        def transformedObservation = HapiHelper.resourceInBundle(bundle, Observation.class)
        def transformedCodingList = transformedObservation.getCode().getCoding()
        transformedCodingList.size() == 2

        // Local code - should remain in the alternate coding
        transformedCodingList[0].code == LOCAL_CODE
        transformedCodingList[0].display == LOCAL_DISPLAY
        transformedCodingList[0].system == HapiHelper.LOINC_CODE
        transformedCodingList[0].extension.size() == 2
        transformedCodingList[0].getExtensionString(HapiHelper.EXTENSION_CWE_CODING) == LOCAL_CODING
        transformedCodingList[0].getExtensionString(HapiHelper.EXTENSION_CODING_SYSTEM) == LOCAL_CODING_SYSTEM

        // LOINC code - should be added as the primary coding
        transformedCodingList[1].code == "85269-9"
        transformedCodingList[1].display == "X-linked Adrenoleukodystrophy (X- ALD) newborn screen interpretation"
        transformedCodingList[1].system == HapiHelper.LOINC_CODE
        transformedCodingList[1].extension.size() == 2
        transformedCodingList[1].getExtensionString(HapiHelper.EXTENSION_CWE_CODING) == "coding"
        transformedCodingList[1].getExtensionString(HapiHelper.EXTENSION_CODING_SYSTEM) == "LN"
    }
}
