package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom

import gov.hhs.cdc.trustedintermediary.ExamplesHelper
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiFhirHelper
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiHelper
import gov.hhs.cdc.trustedintermediary.wrappers.MetricMetadata
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.ServiceRequest


class CopyOrcOrderProviderToObrOrderProviderTest extends Specification{

    def transformClass

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(MetricMetadata, Mock(MetricMetadata))
        TestApplicationContext.injectRegisteredImplementations()

        transformClass = new CopyOrcOrderProviderToObrOrderProvider()
    }

    def "when both practitioner resources populated"() {
        given:
        def fhirResource = ExamplesHelper.getExampleFhirResource("../CA/007_CA_ORU_R01_CDPH_produced_UCSD2024-07-11-16-02-17-749_1_hl7_translation.fhir")
        def bundle = fhirResource.getUnderlyingResource() as Bundle
        def diagnosticReport = HapiHelper.getDiagnosticReport(bundle)
        def serviceRequest = HapiHelper.getServiceRequest(diagnosticReport)
        def practitionerRole = HapiHelper.getPractitionerRole(serviceRequest)
        def practitioner = practitionerRole.practitioner.getResource()

        // practitionerRole.practitioner.getResource().identifier[0].value  <-- the NPI
        // practitionerRole.practitioner.getResource().name[0]              <-- provider's name

        def xcnExtension = practitionerRole.practitioner.getResource().getExtensionByUrl("https://reportstream.cdc.gov/fhir/StructureDefinition/xcn-practitioner")
        def npiLabelText = xcnExtension.getExtensionByUrl("XCN.10").value


        when:
        transformClass.transform(fhirResource, null)
        def result = ""

        then:
        result == "the extension for OBR-16 should have the NPI populated"
    }


    def "when ORC-12 extension populated and OBR-16 extension not populated"() {
        given:
        when:
        def result = ""
        then:
        1 == 0
    }

    def "when ORC-12 extension not populated and OBR-16 extension is populated"() {
        given:
        when:
        def result = ""
        then:
        1 == 0
    }

    def "when neither is populated"() {
        given:
        when:
        def result = ""
        then:
        1 == 0
    }
}

import spock.lang.Specification
