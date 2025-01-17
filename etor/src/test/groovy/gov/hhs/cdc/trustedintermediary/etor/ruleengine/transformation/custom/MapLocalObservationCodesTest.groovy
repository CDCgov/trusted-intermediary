package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom

import gov.hhs.cdc.trustedintermediary.ExamplesHelper
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiFhirHelper
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiFhirResource
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiHelper
import gov.hhs.cdc.trustedintermediary.wrappers.Logger
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Coding
import org.hl7.fhir.r4.model.Extension
import org.hl7.fhir.r4.model.Observation
import org.hl7.fhir.r4.model.StringType
import spock.lang.Specification

class MapLocalObservationCodesTest extends Specification {
    def transformClass
    def mockLogger = Mock(Logger)

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(Logger, mockLogger)
        TestApplicationContext.injectRegisteredImplementations()

        transformClass = new MapLocalObservationCodes()
    }

    def "When message has a mappable local observation code in OBX-3.4/5/6, should add the mapped code to OBX-3.1/2/3"() {
        given:
        def bundle = createBundleWithObservation(initialCode, initialDisplay, true)

        when:
        transformClass.transform(new HapiFhirResource(bundle), getArgs())

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
                HapiHelper.LOCAL_CODE_URL,
                "alt-coding",
                HapiHelper.LOCAL_CODE)

        where:
        initialCode | initialDisplay                                                     || expectedCode | expectedDisplay                                                        | expectedCodingSystem  | expectedExtensionSystem
        "99717-32"  | "Adrenoleukodystrophy deficiency newborn screening interpretation" || "85269-9"    | "X-linked Adrenoleukodystrophy (X- ALD) newborn screen interpretation" | HapiHelper.LOINC_URL  | HapiHelper.LOINC_CODE
        "99717-34"  | "Adrenoleukodystrophy Mutation comments/discussion"                || "PLT325"     | "ABCD1 gene mutation found [Identifier] in DBS by Sequencing"          | null                  | HapiHelper.PLT_CODE
    }

    def "When message has an unmapped local observation code in OBX-3.4/5/6, no mapping should occur and a warning should be logged"() {
        given:
        def bundle = createBundleWithObservation("UNMAPPED", "An unmapped local code", true)
        def originalCodingList = HapiHelper.resourceInBundle(bundle, Observation.class).getCode().getCoding()


        when:
        transformClass.transform(new HapiFhirResource(bundle), getArgs())

        then:
        1 * mockLogger.logWarning(*_)

        def transformedObservation = HapiHelper.resourceInBundle(bundle, Observation.class)
        def transformedCodingList = transformedObservation.getCode().getCoding()
        transformedCodingList.size() == 1

        originalCodingList == transformedCodingList
    }

    def "When message has a mappable local observation code in OBX-3.4/5/6 and other content in OBX3-1/2/3, no mapping should occur"() {
        given:
        def bundle = createBundleWithMultipleCodings(obx31code, obx32display, "99717-32", "Adrenoleukodystrophy deficiency")
        def originalCodingList = HapiHelper.resourceInBundle(bundle, Observation.class).getCode().getCoding()

        when:
        transformClass.transform(new HapiFhirResource(bundle), getArgs())

        then:
        def transformedObservation = HapiHelper.resourceInBundle(bundle, Observation.class)
        def transformedCodingList = transformedObservation.getCode().getCoding()
        transformedCodingList.size() == 2

        originalCodingList == transformedCodingList

        where:
        obx31code    | obx32display
        "some_code"  | "Some display"
        "some_code"  | null
        null         | "Some display"
    }

    def "When message has a LOINC code, no mapping should occur"() {
        given:
        def bundle = createBundleWithObservation("A_LOINC_CODE", "Some display", false)
        def originalCodingList = HapiHelper.resourceInBundle(bundle, Observation.class).getCode().getCoding()

        when:
        transformClass.transform(new HapiFhirResource(bundle), getArgs())

        then:
        def transformedObservation = HapiHelper.resourceInBundle(bundle, Observation.class)
        def transformedCodingList = transformedObservation.getCode().getCoding()
        transformedCodingList.size() == 1

        originalCodingList == transformedCodingList

        where:
        codingSystem << ["coding", "alt-coding"]
    }

    def "When no coding system, no mapping should occur"() {
        given:
        def bundle = createBundleWithNoSystem()
        def originalCodingList = HapiHelper.resourceInBundle(bundle, Observation.class).getCode().getCoding()

        when:
        transformClass.transform(new HapiFhirResource(bundle), getArgs())

        then:
        def transformedObservation = HapiHelper.resourceInBundle(bundle, Observation.class)
        def transformedCodingList = transformedObservation.getCode().getCoding()
        transformedCodingList.size() == 1

        originalCodingList == transformedCodingList
    }

    def "When no coding extension, no mapping should occur"() {
        given:
        def bundle = createBundleWithNoExtension("A_LOCAL_CODE", "The local code description")
        def originalCodingList = HapiHelper.resourceInBundle(bundle, Observation.class).getCode().getCoding()

        when:
        transformClass.transform(new HapiFhirResource(bundle), getArgs())

        then:
        def transformedObservation = HapiHelper.resourceInBundle(bundle, Observation.class)
        def transformedCodingList = transformedObservation.getCode().getCoding()
        transformedCodingList.size() == 1

        originalCodingList == transformedCodingList
    }

    def "When no observation identifier, the observation does not change"() {
        given:
        def bundle = createBundleWithNoIdentifier()
        def originalObservation = HapiHelper.resourceInBundle(bundle, Observation.class)

        when:
        transformClass.transform(new HapiFhirResource(bundle), getArgs())

        then:
        def transformedObservation = HapiHelper.resourceInBundle(bundle, Observation.class)

        originalObservation == transformedObservation
    }

    def "When message has multiple observations, local and non-local codes are handled appropriately"() {
        given:
        final String FHIR_ORU_PATH = "../CA/020_CA_ORU_R01_CDPH_OBX_to_LOINC_1_hl7_translation.fhir"
        def fhirResource = ExamplesHelper.getExampleFhirResource(FHIR_ORU_PATH)
        def bundle = fhirResource.getUnderlyingData() as Bundle
        def initialObservations = HapiHelper.resourcesInBundle(bundle, Observation.class).toList()

        expect:
        initialObservations.size() == 114

        when:
        transformClass.transform(new HapiFhirResource(bundle), getArgs())

        then:
        def transformedObservations = HapiHelper.resourcesInBundle(bundle, Observation.class).toList()
        transformedObservations.size() == 114

        // Assortment of LOINC codes - ensure they are left as-is
        def initialLoinc1 = getObservationByCode(initialObservations, "57721-3")
        def transformedLoinc1 = getObservationByCode(transformedObservations, "57721-3")
        initialLoinc1 == transformedLoinc1

        def initialLoinc2 = getObservationByCode(initialObservations, "8339-4")
        def transformedLoinc2 = getObservationByCode(transformedObservations, "8339-4")
        initialLoinc2 == transformedLoinc2

        def initialLoinc3 = getObservationByCode(initialObservations, "54104-5")
        def transformedLoinc3 = getObservationByCode(transformedObservations, "54104-5")
        initialLoinc3 == transformedLoinc3

        // Mappable local code to LOINC - should have mapped code added
        def mappedLoinc = getObservationByCode(transformedObservations, "99717-33")
        mappedLoinc.code.coding.size() == 2

        evaluateCoding(
                mappedLoinc.code.coding[0],
                "85268-1",
                "X-linked Adrenoleukodystrophy (X- ALD) newborn screening comment-discussion",
                HapiHelper.LOINC_URL,
                "coding",
                HapiHelper.LOINC_CODE)

        evaluateCoding(
                mappedLoinc.code.coding[1],
                "99717-33",
                "Adrenoleukodystrophy deficiency newborn screening comments-discussion",
                HapiHelper.LOCAL_CODE_URL,
                "alt-coding",
                HapiHelper.LOCAL_CODE)

        // Mappable local code to PLT - should have mapped code added
        def mappedPlt = getObservationByCode(transformedObservations, "99717-48")
        mappedPlt.code.coding.size() == 2

        evaluateCoding(
                mappedPlt.code.coding[0],
                "PLT3258",
                "IDUA gene mutations found [Identifier] in DBS by Sequencing",
                null,
                "coding",
                HapiHelper.PLT_CODE)

        evaluateCoding(
                mappedPlt.code.coding[1],
                "99717-48",
                "MPS I IDUA Gene Sequence Mutation Information",
                HapiHelper.LOCAL_CODE_URL,
                "alt-coding",
                HapiHelper.LOCAL_CODE)

        // Unmapped local code - ensure it is left as-is
        def initialAccession = getObservationByCode(initialObservations, "99717-5")
        def transformedAccession = getObservationByCode(transformedObservations, "99717-5")
        initialAccession == transformedAccession
    }

    def "When args are missing coding system, throws a NullPointerException"() {
        given:
        def exceptionMessage = "missing or empty required field codingSystem"
        def bundle = createBundleWithObservation("99717-32", "Adrenoleukodystrophy deficiency newborn screening interpretation", true)
        def args = [
            "codingMap": [
                "99717-32": [
                    "code"        : "85269-9",
                    "display"     : "X-linked Adrenoleukodystrophy (X- ALD) newborn screen interpretation",
                ]
            ]
        ]

        when:
        transformClass.transform(new HapiFhirResource(bundle), args)

        then:
        def exception = thrown(IllegalArgumentException)
        exception.message.contains(exceptionMessage)
    }

    def "When args codingMap is improperly structured, throws a ClassCastException"() {
        given:
        def bundle = createBundleWithObservation("99717-32", "Adrenoleukodystrophy deficiency newborn screening interpretation", true)
        def args = [
            "codingMap": [
                "code" : "99717-32"
            ]
        ]

        when:
        transformClass.transform(new HapiFhirResource(bundle), args)

        then:
        thrown(ClassCastException)
    }

    def "When args codingMap is not present in the args, throws a NullPointerException"() {
        given:
        def exceptionMessage = "argsCodingMap"
        def bundle = createBundleWithObservation("99717-32", "Adrenoleukodystrophy deficiency newborn screening interpretation", true)
        def argsMissingCodingSystem = [
            "theCodingMap": "IsNotHere"
        ]

        when:
        transformClass.transform(new HapiFhirResource(bundle), argsMissingCodingSystem)

        then:
        def exception = thrown(NullPointerException)
        exception.message.contains(exceptionMessage)
    }

    def "When args is null, throws a NullPointerException"() {
        given:
        def exceptionMessage = "args"
        def bundle = createBundleWithObservation("99717-32", "Adrenoleukodystrophy deficiency newborn screening interpretation", true)

        when:
        transformClass.transform(new HapiFhirResource(bundle), null)

        then:
        def exception = thrown(NullPointerException)
        exception.message.contains(exceptionMessage)
    }

    def "When args are missing code, throws a NullPointerException"() {
        given:
        def exceptionMessage = "missing or empty required field code"
        def bundle = createBundleWithObservation("99717-32", "Adrenoleukodystrophy deficiency newborn screening interpretation", true)
        def args = [
            "codingMap": [
                "99717-32": [
                    "display"     : "X-linked Adrenoleukodystrophy (X- ALD) newborn screen interpretation",
                    "codingSystem": "LN",
                ]
            ]
        ]

        when:
        transformClass.transform(new HapiFhirResource(bundle), args)

        then:
        def exception = thrown(IllegalArgumentException)
        exception.message.contains(exceptionMessage)
    }

    def "When args are missing display, throws a NullPointerException"() {
        given:
        def exceptionMessage = "missing or empty required field display"
        def bundle = createBundleWithObservation("99717-32", "Adrenoleukodystrophy deficiency newborn screening interpretation", true)
        def args = [
            "codingMap": [
                "99717-32": [
                    "code"        : "85269-9",
                    "codingSystem": "LN",
                ]
            ]
        ]

        when:
        transformClass.transform(new HapiFhirResource(bundle), args)

        then:
        def exception = thrown(IllegalArgumentException)
        exception.message.contains(exceptionMessage)
    }

    Observation getObservationByCode(List<Observation> observationList, String code) {
        return observationList.find {observation -> observation.code?.coding?.find { coding -> coding.code == code}}
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

    def createBundleWithObservation(String code, String display, boolean isLocal) {
        def bundle = HapiFhirHelper.createMessageBundle(messageTypeCode: 'ORU_R01')
        def observation = new Observation()
        observation.code.addCoding(createCoding(code, display, isLocal, "alt-coding"))
        bundle.addEntry(new Bundle.BundleEntryComponent().setResource(observation))
        return bundle
    }

    def createBundleWithMultipleCodings(String code1, String display1, String code2, String display2) {
        def bundle = HapiFhirHelper.createMessageBundle(messageTypeCode: 'ORU_R01')
        def observation = new Observation()
        observation.code.addCoding(createCoding(code1, display1, false, "coding"))
        observation.code.addCoding(createCoding(code2, display2, true, "alt-coding"))
        bundle.addEntry(new Bundle.BundleEntryComponent().setResource(observation))
        return bundle
    }

    def createBundleWithNoSystem() {
        def bundle = HapiFhirHelper.createMessageBundle(messageTypeCode: 'ORU_R01')
        def observation = new Observation()
        def coding = new Coding()
        coding.code = "A_LOCAL_CODE"
        coding.display = "The local code description"
        coding.addExtension(HapiHelper.EXTENSION_CWE_CODING, new StringType("alt-coding"))
        observation.code.addCoding(coding)
        bundle.addEntry(new Bundle.BundleEntryComponent().setResource(observation))
        return bundle
    }

    def createBundleWithNoExtension(String code, String display) {
        def bundle = HapiFhirHelper.createMessageBundle(messageTypeCode: 'ORU_R01')
        def observation = new Observation()

        def coding = new Coding()
        coding.system = HapiHelper.LOCAL_CODE_URL // System is present, but no extensions
        coding.code = code
        coding.display = display

        observation.code.addCoding(coding)
        bundle.addEntry(new Bundle.BundleEntryComponent().setResource(observation))
        return bundle
    }

    def createBundleWithNoIdentifier() {
        def bundle = HapiFhirHelper.createMessageBundle(messageTypeCode: 'ORU_R01')
        def observation = new Observation()
        observation.status = Observation.ObservationStatus.FINAL
        observation.valueCodeableConcept.coding.add(new Coding(code: "123456"))
        bundle.addEntry(new Bundle.BundleEntryComponent().setResource(observation))
        return bundle
    }

    def createCoding(String code, String display, boolean isLocal, String cweCoding) {
        def coding = new Coding()
        coding.system = isLocal ? HapiHelper.LOCAL_CODE_URL : HapiHelper.LOINC_URL
        coding.code = code
        coding.display = display

        coding.addExtension(new Extension(HapiHelper.EXTENSION_CWE_CODING, new StringType(cweCoding)))
        coding.addExtension(new Extension(HapiHelper.EXTENSION_CODING_SYSTEM, new StringType(isLocal ? HapiHelper.LOCAL_CODE : HapiHelper.LOINC_CODE)))

        return coding
    }

    def getArgs() {
        return [
            "codingMap": [
                "99717-32": [
                    "code"        : "85269-9",
                    "display"     : "X-linked Adrenoleukodystrophy (X- ALD) newborn screen interpretation",
                    "codingSystem": "LN"
                ],
                "99717-33": [
                    "code"        : "85268-1",
                    "display"     : "X-linked Adrenoleukodystrophy (X- ALD) newborn screening comment-discussion",
                    "codingSystem": "LN"
                ],
                "99717-34": [
                    "code"        : "PLT325",
                    "display"     : "ABCD1 gene mutation found [Identifier] in DBS by Sequencing",
                    "codingSystem": "PLT"
                ],
                "99717-48": [
                    "code"        : "PLT3258",
                    "display"     : "IDUA gene mutations found [Identifier] in DBS by Sequencing",
                    "codingSystem": "PLT"
                ]
            ]
        ]
    }
}
