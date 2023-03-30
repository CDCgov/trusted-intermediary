package gov.hhs.cdc.trustedintermediary.external.hapi

import gov.hhs.cdc.trustedintermediary.DemographicsMock
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.etor.demographics.LabOrderConverter
import java.time.Instant
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Identifier
import org.hl7.fhir.r4.model.MessageHeader
import org.hl7.fhir.r4.model.Patient
import org.hl7.fhir.r4.model.Provenance
import org.hl7.fhir.r4.model.ServiceRequest
import spock.lang.Specification

class HapiLabOrderConverterTest extends Specification {

    Patient mockPatient
    Bundle mockDemographicsBundle
    DemographicsMock<Bundle> mockDemographics

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(LabOrderConverter, HapiLabOrderConverter.getInstance())
        TestApplicationContext.injectRegisteredImplementations()

        mockPatient = new Patient()
        mockDemographicsBundle = new Bundle().addEntry(new Bundle.BundleEntryComponent().setResource(mockPatient))
        mockDemographics = new DemographicsMock("fhirResourceId", "patientId", mockDemographicsBundle)
    }

    def "the converter fills in gaps of any missing data in the Bundle"() {

        when:
        def labOrderBundle = HapiLabOrderConverter.getInstance().convertToOrder(mockDemographics).getUnderlyingOrder()

        then:
        labOrderBundle.hasId()
        labOrderBundle.hasIdentifier()
        labOrderBundle.hasTimestamp()
        labOrderBundle.getType() == Bundle.BundleType.MESSAGE
        labOrderBundle.getId() == labOrderBundle.getIdentifier().getValue()
    }

    def "the converter doesn't change things if it is already set"() {
        given:
        def mockId = "an id"
        mockDemographicsBundle.setId(mockId)
        def mockIdentifier = "an identifier"
        mockDemographicsBundle.setIdentifier(new Identifier().setValue(mockIdentifier))
        def mockTimestamp = Date.from(Instant.now().minusSeconds(60))
        mockDemographicsBundle.setTimestamp(mockTimestamp)

        when:
        def labOrderBundle = HapiLabOrderConverter.getInstance().convertToOrder(mockDemographics).getUnderlyingOrder()

        then:
        labOrderBundle.getId() == mockId
        labOrderBundle.getIdentifier().getValue() == mockIdentifier
        labOrderBundle.getTimestamp() == mockTimestamp
        labOrderBundle.getId() != labOrderBundle.getIdentifier().getValue()
    }

    def "the converter always changes the bundle type to message"() {
        given:
        mockDemographicsBundle.setType(Bundle.BundleType.COLLECTION)

        when:
        def labOrderBundle = HapiLabOrderConverter.getInstance().convertToOrder(mockDemographics).getUnderlyingOrder()

        then:
        labOrderBundle.getType() == Bundle.BundleType.MESSAGE
    }

    def "the demographics correctly constructs a message header in the lab order"() {

        when:
        def labOrderBundle = HapiLabOrderConverter.getInstance().convertToOrder(mockDemographics).getUnderlyingOrder()

        then:
        def messageHeader = labOrderBundle.getEntry().get(0).getResource() as MessageHeader

        messageHeader.hasId()
        messageHeader.getMeta().getTag().system[0] == "http://terminology.hl7.org/CodeSystem/v2-0103"
        messageHeader.getMeta().getTag().code[0] == "P"
        messageHeader.getMeta().getTag().display[0] == "Production"
        messageHeader.getEventCoding().getSystem() == "http://terminology.hl7.org/CodeSystem/v2-0003"
        messageHeader.getEventCoding().getCode() == "O21"
        messageHeader.getSource().getName() == "CDC Trusted Intermediary"
        messageHeader.getSource().getEndpoint() == "https://reportstream.cdc.gov/"
    }

    def "the converter correctly reuses the patient from the passed in demographics"() {

        when:
        def labOrderBundle = HapiLabOrderConverter.getInstance().convertToOrder(mockDemographics).getUnderlyingOrder()

        then:
        def patient = labOrderBundle.getEntry().get(1).getResource() as Patient

        patient == mockPatient
    }

    def "the converter correctly constructs a service request in the lab order"() {

        when:
        def labOrderBundle = HapiLabOrderConverter.getInstance().convertToOrder(mockDemographics).getUnderlyingOrder()

        then:
        def serviceRequest = labOrderBundle.getEntry().get(2).getResource() as ServiceRequest

        serviceRequest.hasId()
        serviceRequest.getCode().getCodingFirstRep().getCode() == "54089-8"
        serviceRequest.getCategoryFirstRep().getCodingFirstRep().getCode() == "108252007"
        serviceRequest.getSubject().getResource() == labOrderBundle.getEntry().get(1).getResource()
        serviceRequest.hasAuthoredOn()
    }

    def "the order datetime should match for bundle, service request, and provenance resources"(){

        when:
        def labOrderBundle = HapiLabOrderConverter.getInstance().convertToOrder(demographics).getUnderlyingOrder()
        def bundleDateTime = labOrderBundle.getTimestamp()
        def serviceRequest = labOrderBundle.getEntry().get(2).getResource() as ServiceRequest
        def provenance = labOrderBundle.getEntry().get(3).getResource() as Provenance

        then:
        def serviceRequestDateTime = serviceRequest.getAuthoredOn()
        def provenanceDateTime = provenance.getRecorded()

        bundleDateTime == serviceRequestDateTime
        bundleDateTime == provenanceDateTime
        serviceRequestDateTime == provenanceDateTime
    }
}
