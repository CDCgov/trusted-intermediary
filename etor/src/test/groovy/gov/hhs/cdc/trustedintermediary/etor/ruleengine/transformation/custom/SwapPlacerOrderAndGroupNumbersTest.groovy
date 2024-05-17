package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom

import gov.hhs.cdc.trustedintermediary.ExamplesHelper
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.RuleExecutionException
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiFhirResource
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiHelper
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.ServiceRequest
import spock.lang.Specification

class SwapPlacerOrderAndGroupNumbersTest extends Specification {
    def transformClass

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.injectRegisteredImplementations()

        transformClass = new SwapPlacerOrderAndGroupNumbers()
    }

    def "switch OCR.2 and OCR.4 in Bundle"() {
        given:
        def fhirResource = ExamplesHelper.getExampleFhirResource("../MN/004_MN_ORU_R01_NBS_1_hl7_translation.fhir")
        def bundle = fhirResource.getUnderlyingResource() as Bundle

        def serviceRequest = HapiHelper.resourceInBundle(bundle, ServiceRequest)
        def placerOrderNumberIdentifier = HapiHelper.getPlacerOrderNumberIdentifier(serviceRequest)
        def orc2_1 = HapiHelper.getEI1Value(placerOrderNumberIdentifier)
        def orc2_2 = HapiHelper.getEI2Value(placerOrderNumberIdentifier)
        def placerGroupNumberCoding = HapiHelper.getPlacerGroupNumberCoding(serviceRequest)
        def orc4_1 = placerGroupNumberCoding.getCode()
        def orc4_2 = placerGroupNumberCoding.getDisplay()

        expect:
        orc2_1 == "423787478"
        orc2_2 == "EPIC"
        orc4_1 == "57128-1"
        orc4_2 == "Newborn Screening Report summary panel"

        when:
        transformClass.transform(fhirResource, null)
        def actualPlacerOrderNumberIdentifier = HapiHelper.getPlacerOrderNumberIdentifier(serviceRequest)
        def actualOrc2_1 = HapiHelper.getEI1Value(actualPlacerOrderNumberIdentifier)
        def actualOrc2_2 = HapiHelper.getEI2Value(actualPlacerOrderNumberIdentifier)
        def actualPlacerGroupNumberCoding = HapiHelper.getPlacerGroupNumberCoding(serviceRequest)
        def actualOrc4_1 = actualPlacerGroupNumberCoding.getCode()
        def actualOrc4_2 = actualPlacerGroupNumberCoding.getDisplay()

        then:
        actualOrc2_1 == orc4_1
        actualOrc2_2 == orc4_2
        actualOrc4_1 == orc2_1
        actualOrc4_2 == orc2_2
    }

    def "throw RuleExecutionException if ServiceRequest doesn't have identifiers"() {
        given:
        def bundle = new Bundle()
        bundle.addEntry(new Bundle.BundleEntryComponent().setResource(new ServiceRequest()))

        when:
        transformClass.transform(new HapiFhirResource(bundle), null)

        then:
        thrown(RuleExecutionException)
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
