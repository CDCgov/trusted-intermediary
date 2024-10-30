package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom

import gov.hhs.cdc.trustedintermediary.ExamplesHelper
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiHelper
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.DiagnosticReport
import org.hl7.fhir.r4.model.Observation
import org.hl7.fhir.r4.model.ServiceRequest
import spock.lang.Specification

class RemoveObservationRequestsTest extends Specification {
    def transformClass
    def obr4_1 = "54089-8"
    def args = Map.of("universalServiceIdentifier",  (Object) obr4_1)

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.injectRegisteredImplementations()

        transformClass = new RemoveObservationRequests()
    }

    def "test when universalServiceIdentifier is not a String"() {
        given:
        // Load a FHIR resource example
        def fhirResource = ExamplesHelper.getExampleFhirResource("../CA/002_CA_ORU_R01_initial_translation.fhir")
        def bundle = fhirResource.getUnderlyingData() as Bundle

        // Prepare args with a List<String> instead of a String to trigger null response from ternary operator
        def listOfIdentifiers = ["54089-8", "99717-5"]
        def args = Map.of("universalServiceIdentifier", (Object) listOfIdentifiers)

        when:
        // Call transform with the complex args map
        transformClass.transform(fhirResource, args)

        then:
        thrown(ClassCastException)
    }

    def "remove all LA Ochsner's OBRs except for the one with OBR-4.1 = '54089-8'"() {
        given:
        def fhirResource = ExamplesHelper.getExampleFhirResource("../LA/002_LA_ORU_R01_ORU_removal_test_file.fhir")
        def bundle = fhirResource.getUnderlyingResource() as Bundle

        def initialDiagnosticReports = HapiHelper.resourcesInBundle(bundle, DiagnosticReport).toList()
        def initialServiceRequests = HapiHelper.resourcesInBundle(bundle, ServiceRequest).toList()
        def initialObservations = HapiHelper.resourcesInBundle(bundle, Observation).toList()
        def obr4_1Values = initialServiceRequests.collect { HapiHelper.getOBR4_1Value(it) }

        expect:
        initialDiagnosticReports.size() > 1
        initialServiceRequests.size() > 1
        initialObservations.size() > 1
        obr4_1 in obr4_1Values

        when:
        transformClass.transform(fhirResource, args)
        def diagnosticReports = HapiHelper.resourcesInBundle(bundle, DiagnosticReport).toList()
        def serviceRequests = HapiHelper.resourcesInBundle(bundle, ServiceRequest).toList()
        def dr = diagnosticReports.first() as DiagnosticReport
        def sr = HapiHelper.getServiceRequest(dr)

        then:
        diagnosticReports.size() == 1
        serviceRequests.size() == 1
        HapiHelper.getOBR4_1Value(sr) == obr4_1
    }

    def "remove all CA USCD's OBRs except for the one with OBR-4.1 = '54089-8'"() {
        given:
        def fhirResource = ExamplesHelper.getExampleFhirResource("../CA/002_CA_ORU_R01_initial_translation.fhir")
        def bundle = fhirResource.getUnderlyingData() as Bundle

        def initialDiagnosticReports = HapiHelper.resourcesInBundle(bundle, DiagnosticReport).toList()
        def initialServiceRequests = HapiHelper.resourcesInBundle(bundle, ServiceRequest).toList()
        def initialObservations = HapiHelper.resourcesInBundle(bundle, Observation).toList()
        def obr4_1Values = initialServiceRequests.collect { HapiHelper.getOBR4_1Value(it) }

        expect:
        initialDiagnosticReports.size() > 1
        initialServiceRequests.size() > 1
        initialObservations.size() > 1
        obr4_1 in obr4_1Values

        when:
        transformClass.transform(fhirResource, args)
        def diagnosticReports = HapiHelper.resourcesInBundle(bundle, DiagnosticReport).toList()
        def serviceRequests = HapiHelper.resourcesInBundle(bundle, ServiceRequest).toList()
        def dr = diagnosticReports.first() as DiagnosticReport
        def sr = HapiHelper.getServiceRequest(dr)

        then:
        diagnosticReports.size() == 1
        serviceRequests.size() == 1
        HapiHelper.getOBR4_1Value(sr) == obr4_1
    }

    def "once removed all OBRs except one, attach all observations to that single OBR"() {
        given:
        def fhirResource = ExamplesHelper.getExampleFhirResource("../CA/002_CA_ORU_R01_initial_translation.fhir")
        def bundle = fhirResource.getUnderlyingData() as Bundle

        def initialDiagnosticReports = HapiHelper.resourcesInBundle(bundle, DiagnosticReport).toList()
        def initialObservations = HapiHelper.resourcesInBundle(bundle, Observation).toList()
        def observationCount = initialObservations.size()

        expect:
        initialDiagnosticReports.first().result.size() != observationCount

        when:
        transformClass.transform(fhirResource, args)
        def diagnosticReports = HapiHelper.resourcesInBundle(bundle, DiagnosticReport).toList()
        def dr = diagnosticReports.first() as DiagnosticReport

        then:
        dr.result.size() == observationCount
    }

    def "remove all irrelevant OBRs, with edge case of one DiagnosticReport not having a related ServiceRequest"() {
        given:
        def fhirResource = ExamplesHelper.getExampleFhirResource("../Test/Results/006_CA_ORU_R01_one_diagnostic_report_without_basedOn.fhir")
        def bundle = fhirResource.getUnderlyingData() as Bundle

        def initialDiagnosticReports = HapiHelper.resourcesInBundle(bundle, DiagnosticReport).toList()
        def initialServiceRequests = HapiHelper.resourcesInBundle(bundle, ServiceRequest).toList()
        def initialObservations = HapiHelper.resourcesInBundle(bundle, Observation).toList()

        expect:
        initialDiagnosticReports.size() > 1
        initialServiceRequests.size() > 1
        initialObservations.size() > 1

        when:
        transformClass.transform(fhirResource, args)
        def diagnosticReports = HapiHelper.resourcesInBundle(bundle, DiagnosticReport).toList()
        def serviceRequests = HapiHelper.resourcesInBundle(bundle, ServiceRequest).toList()

        then:
        diagnosticReports.size() == 1
        serviceRequests.size() == 1
    }

    def "remove all irrelevant OBRs, with edge case of all DiagnosticReports not having a related ServiceRequest"() {
        given:
        def fhirResource = ExamplesHelper.getExampleFhirResource("../Test/Results/006_CA_ORU_R01_all_diagnostic_reports_without_basedOn.fhir")
        def bundle = fhirResource.getUnderlyingData() as Bundle

        def initialDiagnosticReports = HapiHelper.resourcesInBundle(bundle, DiagnosticReport).toList()
        def initialServiceRequests = HapiHelper.resourcesInBundle(bundle, ServiceRequest).toList()
        def initialObservations = HapiHelper.resourcesInBundle(bundle, Observation).toList()

        expect:
        initialDiagnosticReports.size() > 1
        initialServiceRequests.size() > 1
        initialObservations.size() > 1

        when:
        transformClass.transform(fhirResource, args)
        def diagnosticReports = HapiHelper.resourcesInBundle(bundle, DiagnosticReport).toList()
        def serviceRequests = HapiHelper.resourcesInBundle(bundle, ServiceRequest).toList()

        then:
        diagnosticReports.size() == initialDiagnosticReports.size()
        serviceRequests.size() == initialServiceRequests.size()
    }

    def "no OBRs are removed because nothing matches the universalServiceIdentifier"() {
        given:
        def fhirResource = ExamplesHelper.getExampleFhirResource("../CA/002_CA_ORU_R01_initial_translation.fhir")
        def bundle = fhirResource.getUnderlyingData() as Bundle

        def initialDiagnosticReports = HapiHelper.resourcesInBundle(bundle, DiagnosticReport).toList()
        def initialServiceRequests = HapiHelper.resourcesInBundle(bundle, ServiceRequest).toList()
        def initialObservations = HapiHelper.resourcesInBundle(bundle, Observation).toList()

        expect:
        initialDiagnosticReports.size() > 1
        initialServiceRequests.size() > 1
        initialObservations.size() > 1

        when:
        transformClass.transform(fhirResource, Map.of("universalServiceIdentifier", "someFakeValue"))
        def diagnosticReports = HapiHelper.resourcesInBundle(bundle, DiagnosticReport).toList()
        def serviceRequests = HapiHelper.resourcesInBundle(bundle, ServiceRequest).toList()

        then:
        diagnosticReports.size() == initialDiagnosticReports.size()
        serviceRequests.size() == initialServiceRequests.size()
    }
}
