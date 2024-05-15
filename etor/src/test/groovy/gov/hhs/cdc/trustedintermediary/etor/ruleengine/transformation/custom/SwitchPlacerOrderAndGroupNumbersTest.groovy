package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom

import gov.hhs.cdc.trustedintermediary.ExamplesHelper
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
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

    def "switch OCR.2 and OCR.4 in Bundle"() {
        given:
        def fhirResource = ExamplesHelper.getExampleFhirResource("../MN/004_MN_ORU_R01_NBS_1_hl7_translation.fhir")
        def bundle = fhirResource.getUnderlyingResource() as Bundle
        def result = getORCSections(bundle)
        def orc2_1 = result[0]
        def orc2_2 = result[1]
        def orc4_1 = result[2]
        def orc4_2 = result[3]

        expect:
        orc2_1 == "423787478"
        orc2_2 == "EPIC"
        orc4_1 == "57128-1"
        orc4_2 == "Newborn Screening Report summary panel"

        when:
        transformClass.transform(fhirResource, null)
        def switchedResult = getORCSections(bundle)
        def switchedObr2_1 = switchedResult[0]
        def switchedObr2_2 = switchedResult[1]
        def switchedObr4_1 = switchedResult[2]
        def switchedObr4_2 = switchedResult[3]

        then:
        switchedObr2_1 == orc4_1
        switchedObr2_2 == orc4_2
        switchedObr4_1 == orc2_1
        switchedObr4_2 == orc2_2
    }

    // Returns a list of values for the OCR sections that need checking in the following order:
    // [2.1, 2.2, 4.1, 4.2]
    def getORCSections(Bundle bundle) {
        def serviceRequest = HapiHelper.resourceInBundle(bundle, ServiceRequest)
        def serviceIdentifier = serviceRequest.getIdentifier()[0]
        var serviceNamespaceExtension =
                serviceRequest
                .getIdentifier()[0]
                .getExtensionByUrl(
                "https://reportstream.cdc.gov/fhir/StructureDefinition/assigning-authority")
                .getExtensionByUrl(
                "https://reportstream.cdc.gov/fhir/StructureDefinition/namespace-id")
        var serviceCoding = serviceRequest.getCode().getCoding().get(0)

        return [
            serviceIdentifier.getValue(),
            serviceNamespaceExtension.getValue().primitiveValue(),
            serviceCoding.getCode(),
            serviceCoding.getDisplay()
        ]
    }
}
