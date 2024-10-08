package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom

import gov.hhs.cdc.trustedintermediary.ExamplesHelper
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiHelper
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.ServiceRequest
import spock.lang.Specification

class UpdateUniversalServiceIdentifierTest extends Specification {
    def transformClass
    def fhirResource
    def args = Map.of(
    "checkValue", "54089-8",
    "codingSystem", "CDPHGSPEAP",
    "alternateId", "CDPHGSPEAP")
    def argsNoAlternateId = [
        "checkValue": "54089-8",
        "codingSystem": "CDPHGSPEAP",
        "alternateId": null] as Map

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.injectRegisteredImplementations()

        transformClass = new UpdateUniversalServiceIdentifier()
        fhirResource = ExamplesHelper.getExampleFhirResource("../Test/Results/005_AL_ORU_R01_NBS_Simplified_1_hl7_translation.fhir")
    }

    def "skip transformation if the coding identifier is missing"() {
        given:
        def bundle = fhirResource.getUnderlyingData() as Bundle
        def result = getObrSections(bundle)[0]
        def obr4_1 = result[0]
        def obr4_3 = result[1]
        def obr4_4 = result[2]

        expect:
        obr4_1 == null
        obr4_3 == null
        obr4_4 == null

        when:
        transformClass.transform(fhirResource, args)
        bundle = fhirResource.getUnderlyingData() as Bundle
        def transformedResult = getObrSections(bundle)[0]
        def transformedObr4_1 = transformedResult[0]
        def transformedObr4_3 = transformedResult[1]
        def transformedObr4_4 = transformedResult[2]

        then:
        transformedObr4_1 == obr4_1
        transformedObr4_3 == obr4_3
        transformedObr4_4 == obr4_4
    }

    def "skip transformation if the coding identifier is not the one we want"() {
        given:
        def bundle = fhirResource.getUnderlyingData() as Bundle
        def result = getObrSections(bundle)[1]
        def obr4_1 = result[0]
        def obr4_3 = result[1]
        def obr4_4 = result[2]

        expect:
        obr4_1 == "11111-1"
        obr4_3 == "NA"
        obr4_4 == "This value should stay"

        when:
        transformClass.transform(fhirResource, args)
        bundle = fhirResource.getUnderlyingData() as Bundle
        def transformedResult = getObrSections(bundle)[1]
        def transformedObr4_1 = transformedResult[0]
        def transformedObr4_3 = transformedResult[1]
        def transformedObr4_4 = transformedResult[2]

        then:
        transformedObr4_1 == obr4_1
        transformedObr4_3 == obr4_3
        transformedObr4_4 == obr4_4
    }

    def "update obr4 values when the code matches"() {
        given:
        def bundle = fhirResource.getUnderlyingData() as Bundle
        def result = getObrSections(bundle)[2]
        def obr4_1 = result[0]
        def obr4_3 = result[1]
        def obr4_4 = result[2]

        expect:
        obr4_1 == "54089-8"
        obr4_3 == "NA"
        obr4_4 == "This value should change"

        when:
        transformClass.transform(fhirResource, args)
        bundle = fhirResource.getUnderlyingData() as Bundle
        def transformedResult = getObrSections(bundle)[2]
        def transformedObr4_1 = transformedResult[0]
        def transformedObr4_3 = transformedResult[1]
        def transformedObr4_4 = transformedResult[2]

        then:
        transformedObr4_1 == obr4_1
        transformedObr4_3 == "CDPHGSPEAP"
        transformedObr4_4 == "CDPHGSPEAP"
    }


    def "update only obr4-1 through obr4-3 values when the code matches and alternate id is null"() {
        given:
        def bundle = fhirResource.getUnderlyingData() as Bundle
        def result = getObrSections(bundle)[2]
        def obr4_1 = result[0]
        def obr4_3 = result[1]
        def obr4_4 = result[2]

        expect:
        obr4_1 == "54089-8"
        obr4_3 == "NA"
        obr4_4 == "This value should change"

        when:
        transformClass.transform(fhirResource, argsNoAlternateId)
        bundle = fhirResource.getUnderlyingData() as Bundle
        def transformedResult = getObrSections(bundle)[2]
        def transformedObr4_1 = transformedResult[0]
        def transformedObr4_3 = transformedResult[1]
        def transformedObr4_4 = transformedResult[2]

        then:
        transformedObr4_1 == obr4_1
        transformedObr4_3 == "CDPHGSPEAP"
        transformedObr4_4 == obr4_4
    }

    def "leave obr4 values unchanged if the code matches and they're already correct"() {
        given:
        def bundle = fhirResource.getUnderlyingData() as Bundle
        def result = getObrSections(bundle)[3]
        def obr4_1 = result[0]
        def obr4_3 = result[1]
        def obr4_4 = result[2]

        expect:
        obr4_1 == "54089-8"
        obr4_3 == "CDPHGSPEAP"
        obr4_4 == "CDPHGSPEAP"

        when:
        transformClass.transform(fhirResource, args)
        bundle = fhirResource.getUnderlyingData() as Bundle
        def transformedResult = getObrSections(bundle)[3]
        def transformedObr4_1 = transformedResult[0]
        def transformedObr4_3 = transformedResult[1]
        def transformedObr4_4 = transformedResult[2]

        then:
        transformedObr4_1 == obr4_1
        transformedObr4_3 == obr4_3
        transformedObr4_4 == obr4_4
    }

    def "update values if the coding identifier is correct but the values are missing"() {
        given:
        def bundle = fhirResource.getUnderlyingData() as Bundle
        def result = getObrSections(bundle)[4]
        def obr4_1 = result[0]
        def obr4_3 = result[1]
        def obr4_4 = result[2]

        expect:
        obr4_1 == "54089-8"
        obr4_3 == null
        obr4_4 == null

        when:
        transformClass.transform(fhirResource, args)
        bundle = fhirResource.getUnderlyingData() as Bundle
        def transformedResult = getObrSections(bundle)[4]
        def transformedObr4_1 = transformedResult[0]
        def transformedObr4_3 = transformedResult[1]
        def transformedObr4_4 = transformedResult[2]

        then:
        transformedObr4_1 == obr4_1
        transformedObr4_3 == "CDPHGSPEAP"
        transformedObr4_4 == "CDPHGSPEAP"
    }

    // Returns a list of values for the OBR sections that need checking in the following order:
    // [4.1, 4.3, 4.4]
    def getObrSections(Bundle bundle) {
        def serviceRequests = HapiHelper.resourcesInBundle(bundle, ServiceRequest.class)
        def out = []

        serviceRequests.forEach {
            def allCodings = it.getCode().getCoding()
            def codingSystemContainer = allCodings[0]

            def codingValue
            def codingSystemLabel
            if (codingSystemContainer == null) {
                codingValue = null
                codingSystemLabel = null
            } else {
                codingValue = codingSystemContainer.getCode()
                codingSystemLabel = codingSystemContainer.getExtensionByUrl(HapiHelper.EXTENSION_CODING_SYSTEM)
                if (codingSystemLabel != null) {
                    codingSystemLabel = codingSystemLabel.getValue().primitiveValue()
                }
            }

            def altCodingContainer = allCodings[1]
            def altCode
            if (altCodingContainer == null) {
                altCode = null
            } else {
                altCode = altCodingContainer.getCode()
            }

            out.add([
                codingValue,
                codingSystemLabel,
                altCode
            ])
        }

        return out
    }
}
