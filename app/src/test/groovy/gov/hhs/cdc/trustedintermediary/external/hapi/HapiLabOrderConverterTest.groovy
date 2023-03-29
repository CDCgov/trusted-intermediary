package gov.hhs.cdc.trustedintermediary.external.hapi

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.etor.demographics.LabOrderConverter
import gov.hhs.cdc.trustedintermediary.etor.demographics.NextOfKin
import gov.hhs.cdc.trustedintermediary.etor.demographics.PatientDemographics
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.DateTimeType
import org.hl7.fhir.r4.model.MessageHeader
import org.hl7.fhir.r4.model.Patient
import org.hl7.fhir.r4.model.Provenance
import org.hl7.fhir.r4.model.ServiceRequest
import spock.lang.Specification

import java.time.ZonedDateTime

class HapiLabOrderConverterTest extends Specification {

    def fhirResourceId = "fhir123id"
    def patientId = "patient123id"
    def firstName = "John"
    def lastName = "Doe"
    def sex = "male"
    def birthDateTime = ZonedDateTime.now()
    def birthOrder = 1
    def race = "Asian"
    def nextOfKinFirstName = "Jaina"
    def nextOfKinLastName = "Solo"
    def nextOfKinPhoneNumber = "555-555-5555"
    def nextOfKin = new NextOfKin(nextOfKinFirstName, nextOfKinLastName, nextOfKinPhoneNumber)
    def demographics = new PatientDemographics(
    fhirResourceId,
    patientId,
    firstName,
    lastName,
    sex,
    birthDateTime,
    birthOrder,
    race,
    nextOfKin
    )

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(LabOrderConverter, HapiLabOrderConverter.getInstance())
        TestApplicationContext.injectRegisteredImplementations()
    }

    def "the demographics correctly constructs the overall bundle in the lab order"() {

        when:
        def labOrderBundle = HapiLabOrderConverter.getInstance().convertToOrder(demographics).getUnderlyingOrder()

        then:
        !labOrderBundle.getId().isEmpty()
        labOrderBundle.getId() == labOrderBundle.getIdentifier().getValue()
        labOrderBundle.getType() == Bundle.BundleType.MESSAGE
    }

    def "the demographics correctly constructs a message header in the lab order"() {

        when:
        def labOrderBundle = HapiLabOrderConverter.getInstance().convertToOrder(demographics).getUnderlyingOrder()

        then:
        def messageHeader = labOrderBundle.getEntry().get(0).getResource() as MessageHeader

        !messageHeader.getId().isEmpty()
        messageHeader.getEventCoding().getSystem() == "http://terminology.hl7.org/CodeSystem/v2-0003"
        messageHeader.getEventCoding().getCode() == "O21"
        messageHeader.getSource().getName() == "CDC Trusted Intermediary"
        messageHeader.getSource().getEndpoint() == "https://reportstream.cdc.gov/"
    }

    def "the demographics correctly constructs a patient in the lab order"() {

        when:
        def labOrderBundle = HapiLabOrderConverter.getInstance().convertToOrder(demographics).getUnderlyingOrder()

        then:
        def patient = labOrderBundle.getEntry().get(1).getResource() as Patient

        patient.getId() == fhirResourceId
        patient.getIdentifierFirstRep().getValue() == patientId
        patient.getNameFirstRep().getFamily() == lastName
        patient.getNameFirstRep().getGiven().get(0).getValue() == firstName
        patient.getGender().toCode() == sex
        patient.getBirthDateElement().getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/patient-birthTime").getValue().toString() == new DateTimeType(Date.from(birthDateTime.toInstant())).toString()
        patient.getMultipleBirthIntegerType().getValue() == birthOrder
        patient.getExtensionByUrl("http://hl7.org/fhir/us/core/StructureDefinition/us-core-race").getExtensionByUrl("text").getValue().toString() == race
        patient.getContactFirstRep().getName().getFamily() == nextOfKinLastName
        patient.getContactFirstRep().getName().getGiven().get(0).getValue() == nextOfKinFirstName
        patient.getContactFirstRep().getTelecomFirstRep().getValue() == nextOfKinPhoneNumber
    }

    def "the demographics correctly constructs a service request in the lab order"() {

        when:
        def labOrderBundle = HapiLabOrderConverter.getInstance().convertToOrder(demographics).getUnderlyingOrder()

        then:
        def serviceRequest = labOrderBundle.getEntry().get(2).getResource() as ServiceRequest

        !serviceRequest.getId().isEmpty()
        serviceRequest.getCode().getCodingFirstRep().getCode() == "54089-8"
        serviceRequest.getCategoryFirstRep().getCodingFirstRep().getCode() == "108252007"
        serviceRequest.getSubject().getResource() == labOrderBundle.getEntry().get(1).getResource()
        serviceRequest.getAuthoredOn() != null
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
