package gov.hhs.cdc.trustedintermediary.external.hapi

import gov.hhs.cdc.trustedintermediary.DemographicsMock
import gov.hhs.cdc.trustedintermediary.OrderMock
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.etor.orders.OrderConverter
import java.time.Instant
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Coding
import org.hl7.fhir.r4.model.Identifier
import org.hl7.fhir.r4.model.MessageHeader
import org.hl7.fhir.r4.model.Patient
import org.hl7.fhir.r4.model.Provenance
import org.hl7.fhir.r4.model.ServiceRequest
import spock.lang.Specification

class HapiOrderConverterTest extends Specification {

    Patient mockPatient
    Bundle mockDemographicsBundle
    DemographicsMock<Bundle> mockDemographics
    Bundle mockOrderBundle
    OrderMock<Bundle> mockOrder

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(OrderConverter, HapiOrderConverter.getInstance())
        TestApplicationContext.injectRegisteredImplementations()

        mockPatient = new Patient()
        mockDemographicsBundle = new Bundle().addEntry(new Bundle.BundleEntryComponent().setResource(mockPatient))
        mockDemographics = new DemographicsMock("fhirResourceId", "patientId", mockDemographicsBundle)

        mockOrderBundle = new Bundle().addEntry(new Bundle.BundleEntryComponent().setResource(mockPatient))
        mockOrder = new OrderMock("fhirResourceId", "patientId", mockOrderBundle)
    }

    def "the converter fills in gaps of any missing data in the Bundle"() {

        when:
        def orderBundle = HapiOrderConverter.getInstance().convertToOrder(mockDemographics).getUnderlyingOrder()

        then:
        orderBundle.hasId()
        orderBundle.hasIdentifier()
        orderBundle.hasTimestamp()
        orderBundle.getType() == Bundle.BundleType.MESSAGE
        orderBundle.getId() == orderBundle.getIdentifier().getValue()
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
        def orderBundle = HapiOrderConverter.getInstance().convertToOrder(mockDemographics).getUnderlyingOrder()

        then:
        orderBundle.getId() == mockId
        orderBundle.getIdentifier().getValue() == mockIdentifier
        orderBundle.getTimestamp() == mockTimestamp
        orderBundle.getId() != orderBundle.getIdentifier().getValue()
    }

    def "the converter always changes the bundle type to message"() {
        given:
        mockDemographicsBundle.setType(Bundle.BundleType.COLLECTION)

        when:
        def orderBundle = HapiOrderConverter.getInstance().convertToOrder(mockDemographics).getUnderlyingOrder()

        then:
        orderBundle.getType() == Bundle.BundleType.MESSAGE
    }

    def "the demographics correctly constructs a message header in the lab order"() {

        when:
        def orderBundle = HapiOrderConverter.getInstance().convertToOrder(mockDemographics).getUnderlyingOrder()

        then:
        def messageHeader = orderBundle.getEntry().get(0).getResource() as MessageHeader

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
        def orderBundle = HapiOrderConverter.getInstance().convertToOrder(mockDemographics).getUnderlyingOrder()

        then:
        def patient = orderBundle.getEntry().get(1).getResource() as Patient

        patient == mockPatient
    }

    def "the converter correctly constructs a service request in the lab order"() {

        when:
        def orderBundle = HapiOrderConverter.getInstance().convertToOrder(mockDemographics).getUnderlyingOrder()

        then:
        def serviceRequest = orderBundle.getEntry().get(2).getResource() as ServiceRequest

        serviceRequest.hasId()
        serviceRequest.getCode().getCodingFirstRep().getCode() == "54089-8"
        serviceRequest.getCategoryFirstRep().getCodingFirstRep().getCode() == "108252007"
        serviceRequest.getSubject().getResource() == orderBundle.getEntry().get(1).getResource()
        serviceRequest.hasAuthoredOn()
    }

    def "the order datetime should match for bundle, service request, and provenance resources"() {

        when:
        def orderBundle = HapiOrderConverter.getInstance().convertToOrder(mockDemographics).getUnderlyingOrder()
        def bundleDateTime = orderBundle.getTimestamp()
        def serviceRequest = orderBundle.getEntry().get(2).getResource() as ServiceRequest
        def provenance = orderBundle.getEntry().get(3).getResource() as Provenance

        then:
        def serviceRequestDateTime = serviceRequest.getAuthoredOn()
        def provenanceDateTime = provenance.getRecorded()

        bundleDateTime == serviceRequestDateTime
        bundleDateTime == provenanceDateTime
        serviceRequestDateTime == provenanceDateTime
    }

    def "convert the pre-existing message header to specify OML"() {
        given:
        mockOrderBundle.addEntry(
                new Bundle.BundleEntryComponent().setResource(
                new MessageHeader().setEvent(new Coding(
                "http://terminology.hl7.org/CodeSystem/v2-0003",
                "O01",
                "ORM"))))

        when:
        def convertedOrderBundle = HapiOrderConverter.getInstance().convertMetadataToOmlOrder(mockOrder).getUnderlyingOrder() as Bundle

        then:
        def convertedMessageHeader = convertedOrderBundle.getEntry().get(1).getResource() as MessageHeader

        convertedMessageHeader.getEventCoding().getCode() == "O21"
        convertedMessageHeader.getEventCoding().getDisplay().contains("OML")
    }

    def "adds the message header to specify OML"() {
        when:
        def convertedOrderBundle = HapiOrderConverter.getInstance().convertMetadataToOmlOrder(mockOrder).getUnderlyingOrder() as Bundle

        then:
        def convertedMessageHeader = convertedOrderBundle.getEntry().get(1).getResource() as MessageHeader

        convertedMessageHeader.getEventCoding().getCode() == "O21"
        convertedMessageHeader.getEventCoding().getDisplay().contains("OML")
    }
}
