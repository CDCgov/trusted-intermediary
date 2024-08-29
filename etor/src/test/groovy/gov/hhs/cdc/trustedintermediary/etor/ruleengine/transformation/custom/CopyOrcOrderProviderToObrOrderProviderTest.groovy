package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom

import gov.hhs.cdc.trustedintermediary.ExamplesHelper
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.FhirResource
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiFhirResource
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiHelper
import gov.hhs.cdc.trustedintermediary.wrappers.MetricMetadata
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.DiagnosticReport
import org.hl7.fhir.r4.model.Practitioner
import org.hl7.fhir.r4.model.ServiceRequest
import spock.lang.Specification


class CopyOrcOrderProviderToObrOrderProviderTest extends Specification{

    final String PRACTITIONER_EXTENSION_URL = "https://reportstream.cdc.gov/fhir/StructureDefinition/xcn-practitioner"

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
        final String EXPECTED_NPI = "1790743185"
        final String EXPECTED_FIRST_NAME = "EUSTRATIA"
        final String EXPECTED_LAST_NAME = "HUBBARD"
        final String EXPECTED_NPI_LABEL = "NPI"
        final String FHIR_ORU_PATH = "../CA/007_CA_ORU_R01_CDPH_produced_UCSD2024-07-11-16-02-17-749_1_hl7_translation.fhir"

        def bundle = createBundle(FHIR_ORU_PATH)
        def serviceRequest = createServiceRequest(bundle)

        expect:
        evaluateOrc12Values(serviceRequest, EXPECTED_NPI, EXPECTED_FIRST_NAME, EXPECTED_LAST_NAME, EXPECTED_NPI_LABEL)
        evaluateObr16Values(serviceRequest, null, EXPECTED_FIRST_NAME, EXPECTED_LAST_NAME, null)

        when:
        transformClass.transform(new HapiFhirResource(bundle), null)

        then:
        evaluateOrc12Values(serviceRequest, EXPECTED_NPI, EXPECTED_FIRST_NAME, EXPECTED_LAST_NAME, EXPECTED_NPI_LABEL)
        evaluateObr16Values(serviceRequest, EXPECTED_NPI, EXPECTED_FIRST_NAME, EXPECTED_LAST_NAME, EXPECTED_NPI_LABEL)
    }

    def "when ORC-12 extension populated and OBR-16 extension not populated"() {
        given:
        final String EXPECTED_NPI = "1790743185"
        final String EXPECTED_FIRST_NAME = "EUSTRATIA"
        final String EXPECTED_LAST_NAME = "HUBBARD"
        final String EXPECTED_NPI_LABEL = "NPI"
        final String FHIR_ORU_PATH = "../CA/021_CA_ORU_R01_CDPH_empty_obr16_UCSD2024-07-11-16-02-17-749_1_hl7_translation.fhir"

        def bundle = createBundle(FHIR_ORU_PATH)
        def serviceRequest = createServiceRequest(bundle)

        expect:
        // ORC12 values to copy
        evaluateOrc12Values(serviceRequest, EXPECTED_NPI, EXPECTED_FIRST_NAME, EXPECTED_LAST_NAME, EXPECTED_NPI_LABEL)

        // OBR16 should not exist initially
        def obr16Practitioner = getObr16ExtensionPractitioner(serviceRequest)
        obr16Practitioner == null

        when:
        transformClass.transform(new HapiFhirResource(bundle), null)

        then:
        // ORC12 values should remain the same
        evaluateOrc12Values(serviceRequest, EXPECTED_NPI, EXPECTED_FIRST_NAME, EXPECTED_LAST_NAME, EXPECTED_NPI_LABEL)
        // OBR16 values should be updated to match ORC12
        evaluateObr16Values(serviceRequest, EXPECTED_NPI, EXPECTED_FIRST_NAME, EXPECTED_LAST_NAME, EXPECTED_NPI_LABEL)
    }

    def "when ORC-12 extension not populated and OBR-16 extension is populated"() {
        given:
        final String EXPECTED_NPI = null
        final String EXPECTED_FIRST_NAME = "EUSTRATIA"
        final String EXPECTED_LAST_NAME = "HUBBARD"
        final String EXPECTED_NPI_LABEL = null
        final String FHIR_ORU_PATH = "../CA/022_CA_ORU_R01_CDPH_empty_orc12_UCSD2024-07-11-16-02-17-749_1_hl7_translation.fhir"

        def bundle = createBundle(FHIR_ORU_PATH)
        def serviceRequest = createServiceRequest(bundle)

        expect:
        evaluateOrc12Values(serviceRequest, null, null, null, null)
        evaluateObr16Values(serviceRequest, EXPECTED_NPI, EXPECTED_FIRST_NAME, EXPECTED_LAST_NAME, EXPECTED_NPI_LABEL)

        when:
        transformClass.transform(new HapiFhirResource(bundle), null)

        then:
        evaluateOrc12Values(serviceRequest, null, null, null, null)
        evaluateObr16Values(serviceRequest, EXPECTED_NPI, EXPECTED_FIRST_NAME, EXPECTED_LAST_NAME, EXPECTED_NPI_LABEL)
    }

    def "when neither is populated"() {
        given:
        final String FHIR_ORU_PATH = "../CA/023_CA_ORU_R01_CDPH_empty_orc12_obr16_UCSD2024-07-11-16-02-17-749_1_hl7_translation.fhir"

        def bundle = createBundle(FHIR_ORU_PATH)
        def serviceRequest = createServiceRequest(bundle)

        expect:
        evaluateOrc12IsNull(serviceRequest)
        evaluateObr16IsNull(serviceRequest)

        when:
        transformClass.transform(new HapiFhirResource(bundle), null)

        then:
        evaluateOrc12IsNull(serviceRequest)
        evaluateObr16IsNull(serviceRequest)
    }

    def "when the OBR extension exists, but the OBR.16 extension does not exist"() {
        // todo
        given:
        when:
        def result = ""
        then:
        1 == 1
    }

    Bundle createBundle(String fhirOruPath) {
        def fhirResource = ExamplesHelper.getExampleFhirResource(fhirOruPath)
        return fhirResource.getUnderlyingResource() as Bundle
    }

    ServiceRequest createServiceRequest(Bundle bundle) {
        def diagnosticReport = HapiHelper.getDiagnosticReport(bundle)
        return HapiHelper.getServiceRequest(diagnosticReport)
    }

    def evaluateOrc12IsNull(ServiceRequest serviceRequest) {
        def practitionerRole = HapiHelper.getPractitionerRole(serviceRequest)
        HapiHelper.getPractitioner(practitionerRole) == null
    }

    def evaluateOrc12Values(ServiceRequest serviceRequest, String expectedNpi, String expectedFirstName, String expectedLastName, String expectedNpiLabel) {
        def practitionerRole = HapiHelper.getPractitionerRole(serviceRequest)
        def practitioner = HapiHelper.getPractitioner(practitionerRole)
        def xcnExtension = practitioner?.getExtensionByUrl(PRACTITIONER_EXTENSION_URL)

        practitioner.identifier[0]?.value == expectedNpi
        xcnExtension.getExtensionByUrl("XCN.3")?.value?.toString() == expectedFirstName
        practitioner.name[0]?.family == expectedLastName
        xcnExtension.getExtensionByUrl("XCN.10")?.value?.toString() == expectedNpiLabel
    }

    def evaluateObr16IsNull(ServiceRequest serviceRequest) {
        getObr16ExtensionPractitioner(serviceRequest) == null
    }

    def evaluateObr16Values(ServiceRequest serviceRequest, String expectedNpi, String expectedFirstName, String expectedLastName, String expectedNpiLabel) {
        def practitioner = getObr16ExtensionPractitioner(serviceRequest)
        def xcnExtension = practitioner.getExtensionByUrl(PRACTITIONER_EXTENSION_URL)

        practitioner.identifier[0]?.value == expectedNpi
        xcnExtension.getExtensionByUrl("XCN.3")?.value?.toString() == expectedFirstName
        practitioner.name[0]?.family == expectedLastName
        xcnExtension.getExtensionByUrl("XCN.10")?.value?.toString() == expectedNpiLabel
    }

    Practitioner getObr16ExtensionPractitioner (serviceRequest) {
        def resource
        try {
            def extensionByUrl1 =  serviceRequest.getExtensionByUrl(HapiHelper.EXTENSION_OBR_URL)
            def extensionByUrl2 = extensionByUrl1.getExtensionByUrl(HapiHelper.EXTENSION_OBR16_DATA_TYPE.toString())
            def value = extensionByUrl2.value
            resource = value.getResource()
            return resource
        } catch(Exception e) {
            resource = null
            return resource
        }
    }
}
