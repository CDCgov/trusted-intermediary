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

    def getObr16ExtensionPractitioner (serviceRequest) {
        return serviceRequest
                .getExtensionByUrl(HapiHelper.EXTENSION_OBR_URL)
                .getExtensionByUrl(HapiHelper.EXTENSION_OBR16_DATA_TYPE.toString())
                .value
                .getResource()
    }

    def "when both practitioner resources populated"() {
        given:
        final String EXPECTED_NPI = "1790743185"
        final String EXPECTED_FIRST_NAME = "EUSTRATIA"
        final String EXPECTED_LAST_NAME = "HUBBARD"
        final String EXPECTED_NPI_LABEL = "NPI"
        final String PRACTITIONER_EXTENSION_URL = "https://reportstream.cdc.gov/fhir/StructureDefinition/xcn-practitioner"

        def fhirResource = ExamplesHelper.getExampleFhirResource("../CA/007_CA_ORU_R01_CDPH_produced_UCSD2024-07-11-16-02-17-749_1_hl7_translation.fhir")
        def bundle = fhirResource.getUnderlyingResource() as Bundle
        def diagnosticReport = HapiHelper.getDiagnosticReport(bundle)
        def serviceRequest = HapiHelper.getServiceRequest(diagnosticReport)

        def orc12PractitionerRole = HapiHelper.getPractitionerRole(serviceRequest)
        def orc12Practitioner = HapiHelper.getPractitioner(orc12PractitionerRole)
        def orc12XcnExtension = orc12Practitioner.getExtensionByUrl(PRACTITIONER_EXTENSION_URL)

        def obr16Practitioner = getObr16ExtensionPractitioner(serviceRequest)
        def obr16XcnExtension = obr16Practitioner.getExtensionByUrl(PRACTITIONER_EXTENSION_URL)

        expect:
        // ORC12 values should remain the same
        orc12Practitioner.identifier[0].value == EXPECTED_NPI
        orc12XcnExtension.getExtensionByUrl("XCN.3").value.toString() == EXPECTED_FIRST_NAME
        orc12Practitioner.name[0].family == EXPECTED_LAST_NAME
        orc12XcnExtension.getExtensionByUrl("XCN.10").value.toString() == EXPECTED_NPI_LABEL

        // OBR16 values should be updated
        obr16Practitioner.identifier[0] == null
        obr16XcnExtension.getExtensionByUrl("XCN.3").value.toString() == EXPECTED_FIRST_NAME
        obr16Practitioner.name[0].family == EXPECTED_LAST_NAME
        obr16XcnExtension.getExtensionByUrl("XCN.10") == null

        when:
        transformClass.transform(new HapiFhirResource(bundle), null)

        then:
        // ORC12 values to copy
        orc12Practitioner.identifier[0].value == EXPECTED_NPI
        orc12XcnExtension.getExtensionByUrl("XCN.3").value.toString() == EXPECTED_FIRST_NAME
        orc12Practitioner.name[0].family == EXPECTED_LAST_NAME
        orc12XcnExtension.getExtensionByUrl("XCN.10").value.toString() == EXPECTED_NPI_LABEL

        // OBR16 original values
        obr16Practitioner.identifier[0].value == EXPECTED_NPI
        obr16XcnExtension.getExtensionByUrl("XCN.3").value.toString() == EXPECTED_FIRST_NAME
        obr16Practitioner.name[0].family == EXPECTED_LAST_NAME
        obr16XcnExtension.getExtensionByUrl("XCN.10").value.toString() == EXPECTED_NPI_LABEL
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
