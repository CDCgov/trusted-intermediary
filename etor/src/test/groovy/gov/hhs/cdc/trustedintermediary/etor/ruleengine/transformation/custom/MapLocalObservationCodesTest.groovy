package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiFhirHelper
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiFhirResource
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiHelper
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Coding
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

    def "When message has a observation code with a LOINC mapping, should add the mapped code to OBX-3.1/2/3"() {
        given:
        final String LOCAL_CODE = "99717-32"
        final String LOCAL_DISPLAY = "Adrenoleukodystrophy deficiency newborn screening interpretation"

        def bundle = HapiFhirHelper.createMessageBundle(messageTypeCode: 'ORU_R01')

        def observation = new Observation()
        observation.code.addCoding(getCoding(LOCAL_CODE, LOCAL_DISPLAY, true, "alt-coding" ))
        bundle.addEntry(new Bundle.BundleEntryComponent().setResource(observation))

        when:
        transformClass.transform(new HapiFhirResource(bundle), null)

        then:
        def transformedObservation = HapiHelper.resourceInBundle(bundle, Observation.class)
        def transformedCodingList = transformedObservation.getCode().getCoding()
        transformedCodingList.size() == 2

        // LOINC code should be added as the primary coding
        evaluateCoding(
                transformedCodingList[0],
                "85269-9",
                "X-linked Adrenoleukodystrophy (X- ALD) newborn screen interpretation",
                HapiHelper.LOINC_CODE,
                "coding",
                "LN")

        // Local code should remain as the alternate coding
        evaluateCoding(
                transformedCodingList[1],
                LOCAL_CODE,
                LOCAL_DISPLAY,
                HapiHelper.LOCALLY_DEFINED_CODE,
                "alt-coding",
                "L")

    }

    def "When message has a observation code with a PLT mapping, should add the mapped code to OBX-3.1/2/3"() {
        given:
        final String LOCAL_CODE = "99717-34"
        final String LOCAL_DISPLAY = "Adrenoleukodystrophy Mutation comments/discussion"

        def bundle = HapiFhirHelper.createMessageBundle(messageTypeCode: 'ORU_R01')

        def observation = new Observation()
        observation.code.addCoding(getCoding(LOCAL_CODE, LOCAL_DISPLAY, true, "alt-coding" ))
        bundle.addEntry(new Bundle.BundleEntryComponent().setResource(observation))

        when:
        transformClass.transform(new HapiFhirResource(bundle), null)

        then:
        def transformedObservation = HapiHelper.resourceInBundle(bundle, Observation.class)
        def transformedCodingList = transformedObservation.getCode().getCoding()
        transformedCodingList.size() == 2

        // PLT code should be added as the primary coding
        evaluateCoding(
                transformedCodingList[0],
                "PLT325",
                "ABCD1 gene mutation found [Identifier] in DBS by Sequencing",
                null,
                "coding",
                "PLT")

        // Local code should remain as the alternate coding
        evaluateCoding(
                transformedCodingList[1],
                LOCAL_CODE,
                LOCAL_DISPLAY,
                HapiHelper.LOCALLY_DEFINED_CODE,
                "alt-coding",
                "L")
    }

    Coding getCoding(String code, String display, boolean localCoding, String cweCoding) {
        def coding = new Coding()
        coding.system = localCoding ? HapiHelper.LOCALLY_DEFINED_CODE : HapiHelper.LOINC_CODE
        coding.code = code
        coding.display = display

        coding.addExtension(HapiHelper.EXTENSION_CWE_CODING, new StringType(cweCoding))
        coding.addExtension(HapiHelper.EXTENSION_CODING_SYSTEM, new StringType(localCoding ? "L" : "LN"))
        return coding
    }

    def evaluateCoding(
            Coding coding,
            String expectedCode,
            String expectedDisplay,
            String expectedSystem,
            String expectedExtensionCoding,
            String expectedExtensionSystem) {
        coding.code == expectedCode
        coding.display == expectedDisplay
        coding.system == expectedSystem
        coding.extension.size() == 2
        coding.getExtensionString(HapiHelper.EXTENSION_CWE_CODING) == expectedExtensionCoding
        coding.getExtensionString(HapiHelper.EXTENSION_CODING_SYSTEM) == expectedExtensionSystem
    }
}
