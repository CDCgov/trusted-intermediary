package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom

import gov.hhs.cdc.trustedintermediary.ExamplesHelper
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiFhirResource
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiHelper
import gov.hhs.cdc.trustedintermediary.wrappers.MetricMetadata
import org.hl7.fhir.r4.model.Bundle


class CopyOrcOrderProviderToObrOrderProviderTest extends Specification{

    def transformClass

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(MetricMetadata, Mock(MetricMetadata))
        TestApplicationContext.injectRegisteredImplementations()

        transformClass = new CopyOrcOrderProviderToObrOrderProvider()
    }

    def getExtensionPractitioner (serviceRequest) {
        return serviceRequest
                .getExtensionByUrl(HapiHelper.EXTENSION_OBR_URL)
                .getExtensionByUrl(HapiHelper.EXTENSION_OBR16_DATA_TYPE.toString())
                .value
                .getResource()
    }

    def "when both practitioner resources populated"() {
        given:
        def fhirResource = ExamplesHelper.getExampleFhirResource("../CA/007_CA_ORU_R01_CDPH_produced_UCSD2024-07-11-16-02-17-749_1_hl7_translation.fhir")
        def bundle = fhirResource.getUnderlyingResource() as Bundle
        def diagnosticReport = HapiHelper.getDiagnosticReport(bundle)
        def serviceRequest = HapiHelper.getServiceRequest(diagnosticReport)

        // practitionerRole.practitioner.getResource().identifier[0].value  <-- the NPI
        // practitionerRole.practitioner.getResource().name[0]              <-- provider's name
        def practitionerRole = HapiHelper.getPractitionerRole(serviceRequest)
        def orc12Practitioner = HapiHelper.getPractitioner(practitionerRole)
        def orc12XcnExtension = orc12Practitioner.getExtensionByUrl("https://reportstream.cdc.gov/fhir/StructureDefinition/xcn-practitioner")
        def obr16Practitioner = getExtensionPractitioner(serviceRequest)
        def obr16XcnExtension = obr16Practitioner.getExtensionByUrl("https://reportstream.cdc.gov/fhir/StructureDefinition/xcn-practitioner")

        expect:
        // ORC12 values to copy
        orc12XcnExtension.getExtensionByUrl("XCN.10").value.toString() == "NPI"
        orc12XcnExtension.getExtensionByUrl("XCN.3").value.toString() == "EUSTRATIA"
        orc12Practitioner.name[0].family == "HUBBARD"
        orc12Practitioner.identifier[0].value == "1790743185"

        // OBR16 original values
        obr16XcnExtension.getExtensionByUrl("XCN.10") == null
        obr16XcnExtension.getExtensionByUrl("XCN.3").value.toString() == "EUSTRATIA"
        obr16Practitioner.name[0].family == "HUBBARD"
        obr16Practitioner.identifier[0] == null

        when:
        transformClass.transform(new HapiFhirResource(bundle), null)

        then:
        // ORC12 values to copy
        orc12XcnExtension.getExtensionByUrl("XCN.10").value.toString() == "NPI"
        orc12XcnExtension.getExtensionByUrl("XCN.3").value.toString() == "EUSTRATIA"
        orc12Practitioner.name[0].family == "HUBBARD"
        orc12Practitioner.identifier[0].value == "1790743185"

        // OBR16 original values
        obr16XcnExtension.getExtensionByUrl("XCN.10").value.toString() == "NPI"
        obr16XcnExtension.getExtensionByUrl("XCN.3").value.toString() == "EUSTRATIA"
        obr16Practitioner.name[0].family == "HUBBARD"
        obr16Practitioner.identifier[0].value == "1790743185"
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
