package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom

import gov.hhs.cdc.trustedintermediary.ExamplesHelper
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiFhirHelper
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiFhirResource
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiHelper
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Coding
import org.hl7.fhir.r4.model.Observation
import org.hl7.fhir.r4.model.StringType
import spock.lang.Specification

class RemoveObservationByCodeTest extends Specification {
    def transformClass

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.injectRegisteredImplementations()

        transformClass = new RemoveObservationByCode()
    }

    def "When an observation has the desired coding, it should be removed"() {
        given:
        def bundle = HapiFhirHelper.createMessageBundle(messageTypeCode: 'ORU_R01')
        def observation = new Observation()
        addCodingToObservation(observation, code, codingSystemExt, codingExt)
        bundle.addEntry(new Bundle.BundleEntryComponent().setResource(observation))

        def args = getArgs(code, codingSystemExt, codingExt)

        expect:
        HapiHelper.resourcesInBundle(bundle, Observation.class).count() == 1

        when:
        transformClass.transform(new HapiFhirResource(bundle), args)

        then:
        HapiHelper.resourcesInBundle(bundle, Observation.class).count() == 0

        where:
        code      | codingSystemExt | codingExt
        "99717-5" | "L"             | "alt-coding"
        "my_code" | "MY_SYS"        | "coding"
    }

    def "When an observation with >1 coding has the desired coding, it should be removed"() {
        given:
        final String MATCHING_CODE = "99717-5"
        final String MATCHING_CODING_SYSTEM_EXT = "L"
        final String MATCHING_CODING_EXT = "alt-coding"

        def bundle = HapiFhirHelper.createMessageBundle(messageTypeCode: 'ORU_R01')
        def observation = new Observation()

        addCodingToObservation(observation, "ANOTHER_CODE", "ANOTHER_SYSTEM", "coding")
        addCodingToObservation(observation, MATCHING_CODE, MATCHING_CODING_SYSTEM_EXT, MATCHING_CODING_EXT)
        bundle.addEntry(new Bundle.BundleEntryComponent().setResource(observation))

        def args = getArgs(MATCHING_CODE, MATCHING_CODING_SYSTEM_EXT, MATCHING_CODING_EXT)

        expect:
        HapiHelper.resourcesInBundle(bundle, Observation.class).count() == 1

        when:
        transformClass.transform(new HapiFhirResource(bundle), args)

        then:
        HapiHelper.resourcesInBundle(bundle, Observation.class).count() == 0
    }

    def "When an observation has coding that's only a partial match, it should NOT be removed"() {
        given:
        final String MATCHING_CODE = "99717-5"
        final String MATCHING_CODING_SYSTEM_EXT = "L"
        final String MATCHING_CODING_EXT = "alt-coding"

        def bundle = HapiFhirHelper.createMessageBundle(messageTypeCode: 'ORU_R01')
        def observation = new Observation()
        addCodingToObservation(observation, code, codingSystemExt, codingExt)
        bundle.addEntry(new Bundle.BundleEntryComponent().setResource(observation))

        def args = getArgs(MATCHING_CODE, MATCHING_CODING_SYSTEM_EXT, MATCHING_CODING_EXT)

        expect:
        HapiHelper.resourcesInBundle(bundle, Observation.class).count() == 1

        when:
        transformClass.transform(new HapiFhirResource(bundle), args)

        then:
        HapiHelper.resourcesInBundle(bundle, Observation.class).count() == 1

        where:
        code      | codingSystemExt | codingExt
        "11111-1" | "L"             | "alt-coding"
        "99717-5" | "DIFFERENT_SYS" | "alt-coding"
        "99717-5" | "L"             | "coding"
    }

    def "When an observation has no identifier OBX-3, it should NOT be removed"() {
        given:
        def bundle = HapiFhirHelper.createMessageBundle(messageTypeCode: 'ORU_R01')

        // Add an observation with an observation value and a status, but no observation identifier
        def observation = new Observation()
        observation.status = Observation.ObservationStatus.FINAL
        def valueCoding = new Coding()
        valueCoding.code = "123456"
        observation.valueCodeableConcept.coding.add(valueCoding)
        bundle.addEntry(new Bundle.BundleEntryComponent().setResource(observation))

        def args = getArgs("55555-5", "LN", "coding")

        expect:
        HapiHelper.resourcesInBundle(bundle, Observation.class).count() == 1

        when:
        transformClass.transform(new HapiFhirResource(bundle), args)

        then:
        HapiHelper.resourcesInBundle(bundle, Observation.class).count() == 1
    }

    def "When there is >1 matching observation, all matching observations should be removed"() {
        given:
        final String MATCHING_CODE = "99717-5"
        final String MATCHING_CODING_SYSTEM_EXT = "L"
        final String MATCHING_CODING_EXT = "alt-coding"

        def bundle = HapiFhirHelper.createMessageBundle(messageTypeCode: 'ORU_R01')

        def observation1 = new Observation()
        def observation2 = new Observation()

        addCodingToObservation(observation1, MATCHING_CODE, MATCHING_CODING_SYSTEM_EXT, MATCHING_CODING_EXT)
        addCodingToObservation(observation2, MATCHING_CODE, MATCHING_CODING_SYSTEM_EXT, MATCHING_CODING_EXT)

        bundle.addEntry(new Bundle.BundleEntryComponent().setResource(observation1))
        bundle.addEntry(new Bundle.BundleEntryComponent().setResource(observation2))

        def args = getArgs(MATCHING_CODE, MATCHING_CODING_SYSTEM_EXT, MATCHING_CODING_EXT)

        expect:
        HapiHelper.resourcesInBundle(bundle, Observation.class).count() == 2

        when:
        transformClass.transform(new HapiFhirResource(bundle), args)

        then:
        HapiHelper.resourcesInBundle(bundle, Observation.class).count() == 0
    }

    def "When message has multiple observations with only 1 matching, only 1 is removed"() {
        given:
        final String MATCHING_CODE = "99717-5"
        final String MATCHING_CODING_SYSTEM_EXT = "L"
        final String MATCHING_CODING_EXT = "alt-coding"

        final String FHIR_ORU_PATH = "../CA/020_CA_ORU_R01_CDPH_OBX_to_LOINC_1_hl7_translation.fhir"
        def fhirResource = ExamplesHelper.getExampleFhirResource(FHIR_ORU_PATH)
        def bundle = fhirResource.getUnderlyingResource() as Bundle

        def args = getArgs(MATCHING_CODE, MATCHING_CODING_SYSTEM_EXT, MATCHING_CODING_EXT)

        expect:
        HapiHelper.resourcesInBundle(bundle, Observation.class).count() == 114

        when:
        transformClass.transform(new HapiFhirResource(bundle), args)

        then:
        HapiHelper.resourcesInBundle(bundle, Observation.class).count() == 113
    }

    void addCodingToObservation(Observation observation, String code, String codingSystemExtension, String codingExtension) {
        def coding = new Coding()

        coding.code = code
        coding.addExtension(HapiHelper.EXTENSION_CODING_SYSTEM, new StringType(codingSystemExtension))
        coding.addExtension(HapiHelper.EXTENSION_CWE_CODING, new StringType(codingExtension))
        observation.code.addCoding(coding)
    }

    Map<String, String> getArgs(String code, String codingSystem, String coding) {
        return [
            "code"                  : code,
            "codingSystemExtension" : codingSystem,
            codingExtension         : coding]
    }
}
