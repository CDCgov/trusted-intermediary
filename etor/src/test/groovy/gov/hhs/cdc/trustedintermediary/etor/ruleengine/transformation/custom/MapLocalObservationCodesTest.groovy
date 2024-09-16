package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom

import gov.hhs.cdc.trustedintermediary.ExamplesHelper
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiFhirHelper
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiFhirResource
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiHelper
import gov.hhs.cdc.trustedintermediary.wrappers.Logger
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Coding
import org.hl7.fhir.r4.model.Observation
import org.hl7.fhir.r4.model.StringType
import spock.lang.Specification

class MapLocalObservationCodesTest extends Specification {
    def transformClass
    def mockLogger = Mock(Logger)

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.injectRegisteredImplementations()
        TestApplicationContext.register(Logger, mockLogger)

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

    def "When message has an unmapped local observation code in OBX-3.4/5/6, no mapping should occur and a warning should be logged"() {
        given:
        final String LOCAL_CODE = "UNMAPPED"
        final String LOCAL_DISPLAY = "An unmapped local code"
        def bundle = HapiFhirHelper.createMessageBundle(messageTypeCode: 'ORU_R01')

        def observation = new Observation()
        observation.code.addCoding(getCoding(LOCAL_CODE, LOCAL_DISPLAY, true, "alt-coding" ))
        bundle.addEntry(new Bundle.BundleEntryComponent().setResource(observation))

        when:
        transformClass.transform(new HapiFhirResource(bundle), null)

        then:
        1 * mockLogger.logWarning(_ as String)

        def transformedObservation = HapiHelper.resourceInBundle(bundle, Observation.class)
        def transformedCodingList = transformedObservation.getCode().getCoding()
        transformedCodingList.size() == 1

        evaluateCoding(
                transformedCodingList[0],
                LOCAL_CODE,
                LOCAL_DISPLAY,
                HapiHelper.LOCALLY_DEFINED_CODE,
                "alt-coding",
                "L")
    }

    def "When message has a mappable local observation code in OBX-3.4/5/6 and other content in OBX3-1/2/3, no mapping should occur"() {
        given:
        def bundle = HapiFhirHelper.createMessageBundle(messageTypeCode: 'ORU_R01')

        def observation = new Observation()
        observation.code.addCoding(getCoding(obx31code, obx32display, false, "coding" ))
        observation.code.addCoding(getCoding("99717-32", "Adrenoleukodystrophy deficiency newborn screening interpretation", true, "alt-coding" ))

        bundle.addEntry(new Bundle.BundleEntryComponent().setResource(observation))

        when:
        transformClass.transform(new HapiFhirResource(bundle), null)

        then:
        def transformedObservation = HapiHelper.resourceInBundle(bundle, Observation.class)
        def transformedCodingList = transformedObservation.getCode().getCoding()
        transformedCodingList.size() == 2

        // Mapped code should be added as the primary coding
        observation.code.coding == transformedCodingList

        where:
        obx31code    | obx32display
        "some_code"  | "Some display"
        "some_code"  | null
        null         | "Some display"
    }

    def "When message has a LOINC code, no mapping should occur"() {
        given:
        final String CODE = "A_LOINC_CODE"
        final String DISPLAY = "Some display"

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

    def "When no observation identifier, the observation does not change"() {
        given:
        def bundle = HapiFhirHelper.createMessageBundle(messageTypeCode: 'ORU_R01')

        // Add an observation with an observation value and a status, but no observation identifier
        def observation = new Observation()
        observation.status = Observation.ObservationStatus.FINAL
        def valueCoding = new Coding()
        valueCoding.code = "123456"
        observation.valueCodeableConcept.coding.add(valueCoding)
        bundle.addEntry(new Bundle.BundleEntryComponent().setResource(observation))

        when:
        transformClass.transform(new HapiFhirResource(bundle), null)

        then:
        def transformedObservation = HapiHelper.resourceInBundle(bundle, Observation.class)

        observation == transformedObservation
    }

    def "When message has multiple observations, local and non-local codes are handled appropriately"() {
        given:
        final String FHIR_ORU_PATH = "../CA/020_CA_ORU_R01_CDPH_OBX_to_LOINC_1_hl7_translation.fhir"
        def fhirResource = ExamplesHelper.getExampleFhirResource(FHIR_ORU_PATH)
        def bundle = fhirResource.getUnderlyingResource() as Bundle
        def initialObservations = HapiHelper.resourcesInBundle(bundle, Observation.class)

        expect:
        initialObservations.count() == 114

        when:
        transformClass.transform(new HapiFhirResource(bundle), null)

        then:
        def transformedObservations = HapiHelper.resourcesInBundle(bundle, Observation.class)
        transformedObservations.count() == 114

        // Look up the first and a few other LOINC codes. Ensure they are present and there is no alt-coding
        // 57721-3
        // 8339-4
        // 54104-5

        // mapped LOINC - ensure evaluateCoding is correct
        // 99717-33 to 85268-1

        // mapped PLT - ensure evaluateCoding is correct
        // 99717-48 to PLT3258

        // unmapped - ensure it's left as-is
        // 99717-5^Accession Number^L
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
        assert coding.system == expectedSystem
        assert coding.extension.size() == 2
        assert coding.getExtensionString(HapiHelper.EXTENSION_CWE_CODING) == expectedExtensionCoding
        assert coding.getExtensionString(HapiHelper.EXTENSION_CODING_SYSTEM) == expectedExtensionSystem
    }
}
