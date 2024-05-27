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

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.injectRegisteredImplementations()

        transformClass = new RemoveObservationRequests()
    }

    def "remove all OBRs except for the one with OBR-4.1 = '54089-8' and attach all observations to the single OBR"() {
        given:
        def fhirResource = ExamplesHelper.getExampleFhirResource("../CA/002_CA_ORU_R01_initial_translation.fhir")
        def bundle = fhirResource.getUnderlyingResource() as Bundle

        def initialDiagnosticReports = HapiHelper.resourcesInBundle(bundle, DiagnosticReport).toList()
        def initialServiceRequests = HapiHelper.resourcesInBundle(bundle, ServiceRequest).toList()
        def initialObservations = HapiHelper.resourcesInBundle(bundle, Observation).toList()
        def observationCount = initialObservations.size()
        def obr4_1Values = initialServiceRequests.collect { HapiHelper.getOBR4_1Value(it) }

        expect:
        initialDiagnosticReports.size() > 1
        initialServiceRequests.size() > 1
        initialObservations.size() > 1
        initialDiagnosticReports.first().result.size() != observationCount
        "54089-8" in obr4_1Values

        when:
        transformClass.transform(fhirResource, null)
        def diagnosticReports = HapiHelper.resourcesInBundle(bundle, DiagnosticReport).toList()
        def serviceRequests = HapiHelper.resourcesInBundle(bundle, ServiceRequest).toList()
        def dr = diagnosticReports.first() as DiagnosticReport
        def sr = HapiHelper.getServiceRequest(dr)

        then:
        diagnosticReports.size() == 1
        serviceRequests.size() == 1
        HapiHelper.getOBR4_1Value(sr) == "54089-8"
        dr.result.size() == observationCount
    }
}
