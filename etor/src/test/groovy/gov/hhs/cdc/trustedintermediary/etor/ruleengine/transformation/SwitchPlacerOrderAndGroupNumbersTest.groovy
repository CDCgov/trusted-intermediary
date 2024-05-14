package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation

import gov.hhs.cdc.trustedintermediary.ExamplesHelper
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom.switchPlacerOrderAndGroupNumbers
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiHelper
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.ServiceRequest
import spock.lang.Specification

class SwitchPlacerOrderAndGroupNumbersTest extends Specification {
    def transformClass

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.injectRegisteredImplementations()

        transformClass = new switchPlacerOrderAndGroupNumbers()
    }

    def "switch OBR.2 and OBR.4 in Bundle"() {
        given:
        def fhirResource = ExamplesHelper.getExampleFhirResource("../MN/004_MN_ORU_R01_NBS_1_hl7_translation.fhir")
        def bundle = fhirResource.getUnderlyingResource() as Bundle
        def result = getOBRSections(bundle)

        def obr2_1 = result[0]
        def obr2_2 = result[1]
        def obr4_1 = result[2]
        def obr4_2 = result[3]

        expect:
        obr2_1 != null
        obr2_2 != null
        obr4_1 != null
        obr4_2 != null

        when:
        transformClass.transform(fhirResource, null)
        def switchedResult = getOBRSections(bundle)
        def switchedObr2_1 = switchedResult[0]
        def switchedObr2_2 = switchedResult[1]
        def switchedObr4_1 = switchedResult[2]
        def switchedObr4_2 = switchedResult[3]

        then:
        switchedObr2_1 != obr2_1
        switchedObr2_2 != obr2_2
        switchedObr4_1 != obr4_1
        switchedObr4_2 != obr4_2
    }

    // Returns a list of values for the OBR sections that need checking in the following order:
    // [2.1, 2.2, 4.1, 4.2]
    def getOBRSections(Bundle bundle) {
        def serviceRequest = HapiHelper.resourcesInBundle(bundle, ServiceRequest)
        def serviceIdentifier = serviceRequest.getIdentifier().get(0)
        var serviceNamespaceExtension =
                serviceRequest
                .getIdentifier()
                .get(0)
                .getExtensionByUrl(
                "https://reportstream.cdc.gov/fhir/StructureDefinition/namespace-id")
        var serviceCoding = serviceRequest.getCode().getCoding().get(0)

        return [
            serviceIdentifier.getValue(),
            serviceNamespaceExtension.getValue().primitiveValue(),
            serviceCoding.getCode(),
            serviceCoding.getDisplayElement()
        ]
    }
}
