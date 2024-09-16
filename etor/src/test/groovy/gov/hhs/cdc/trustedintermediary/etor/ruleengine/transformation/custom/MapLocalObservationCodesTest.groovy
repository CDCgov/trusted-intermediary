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

    def "When message has a mappable local observation code in OBX-3.4/5/6, should add the mapped code to OBX-3.1/2/3"() {
        given:
        def bundle = HapiFhirHelper.createMessageBundle(messageTypeCode: 'ORU_R01')

        def observation = new Observation()
        observation.code.addCoding(getCoding(initialCode, initialDisplay, true, "alt-coding" ))
        bundle.addEntry(new Bundle.BundleEntryComponent().setResource(observation))

        when:
        transformClass.transform(new HapiFhirResource(bundle), null)

        then:
        def transformedObservation = HapiHelper.resourceInBundle(bundle, Observation.class)
        def transformedCodingList = transformedObservation.getCode().getCoding()
        transformedCodingList.size() == 2

        // Mapped code should be added as the primary coding
        evaluateCoding(
                transformedCodingList[0],
                expectedCode,
                expectedDisplay,
                expectedCodingSystem,
                "coding",
                expectedExtensionSystem)

        // Local code should remain as the alternate coding
        evaluateCoding(
                transformedCodingList[1],
                initialCode,
                initialDisplay,
                HapiHelper.LOCALLY_DEFINED_CODE,
                "alt-coding",
                "L")

        where:
        initialCode | initialDisplay                                                     || expectedCode | expectedDisplay                                                        | expectedCodingSystem  | expectedExtensionSystem
        "99717-32"  | "Adrenoleukodystrophy deficiency newborn screening interpretation" || "85269-9"    | "X-linked Adrenoleukodystrophy (X- ALD) newborn screen interpretation" | HapiHelper.LOINC_CODE | "LN"
        "99717-34"  | "Adrenoleukodystrophy Mutation comments/discussion"                || "PLT325"     | "ABCD1 gene mutation found [Identifier] in DBS by Sequencing"          | null                  | "PLT"
    }

    def "When message has a LOINC code, no mapping should occur"() {
        given:
        final String CODE = "A_LOINC_CODE"
        final String DISPLAY = "Some description"

        def bundle = HapiFhirHelper.createMessageBundle(messageTypeCode: 'ORU_R01')

        def observation = new Observation()
        observation.code.addCoding(getCoding(CODE, DISPLAY, false, codingSystem ))
        bundle.addEntry(new Bundle.BundleEntryComponent().setResource(observation))

        when:
        transformClass.transform(new HapiFhirResource(bundle), null)

        then:
        def transformedObservation = HapiHelper.resourceInBundle(bundle, Observation.class)
        def transformedCodingList = transformedObservation.getCode().getCoding()
        transformedCodingList.size() == 1

        // Code should remain as the alternate coding
        evaluateCoding(
                transformedCodingList[0],
                CODE,
                DISPLAY,
                HapiHelper.LOINC_CODE,
                codingSystem,
                "LN")

        where:
        codingSystem << ["coding", "alt-coding"]
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

    void evaluateCoding(
            Coding coding,
            String expectedCode,
            String expectedDisplay,
            String expectedSystem,
            String expectedExtensionCoding,
            String expectedExtensionSystem) {
        assert coding.code == expectedCode
        assert coding.display == expectedDisplay
        coding.system == expectedSystem
        assert coding.extension.size() == 2
        assert coding.getExtensionString(HapiHelper.EXTENSION_CWE_CODING) == expectedExtensionCoding
        assert coding.getExtensionString(HapiHelper.EXTENSION_CODING_SYSTEM) == expectedExtensionSystem
    }
}
