package gov.hhs.cdc.trustedintermediary.external.hapi

import gov.hhs.cdc.trustedintermediary.DemographicsMock
import gov.hhs.cdc.trustedintermediary.OrderMock
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext

import org.hl7.fhir.r4.model.Address
import org.hl7.fhir.r4.model.ContactPoint
import org.hl7.fhir.r4.model.Extension
import org.hl7.fhir.r4.model.HumanName
import org.hl7.fhir.r4.model.StringType

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
        TestApplicationContext.register(HapiMessageConverterHelper, HapiMessageConverterHelper.getInstance())
        TestApplicationContext.register(HapiMessageHelper, HapiMessageHelper.getInstance())
        TestApplicationContext.injectRegisteredImplementations()

        mockPatient = new Patient()
        mockDemographicsBundle = new Bundle().addEntry(new Bundle.BundleEntryComponent().setResource(mockPatient))
        mockDemographics = new DemographicsMock("fhirResourceId", "patientId", mockDemographicsBundle)

        mockOrderBundle = new Bundle().addEntry(new Bundle.BundleEntryComponent().setResource(mockPatient))
        mockOrder = new OrderMock("fhirResourceId", "patientId", mockOrderBundle, null, null, null, null, null)
    }

    def "the converter fills in gaps of any missing data in the Bundle"() {
        given:
        def mockDemographicsResource = mockDemographics.getUnderlyingResource()

        when:
        HapiOrderConverter.convertDemographicsToOrder(mockDemographicsResource)

        then:
        mockDemographicsResource
        mockDemographicsResource.hasIdentifier()
        mockDemographicsResource.hasTimestamp()
        mockDemographicsResource.getType() == Bundle.BundleType.MESSAGE
        mockDemographicsResource.getId() == mockDemographicsResource.getIdentifier().getValue()
    }

    def "the converter doesn't change things if it is already set"() {
        given:
        def mockDemographicsResource = mockDemographics.getUnderlyingResource()
        def mockId = "an id"
        mockDemographicsBundle.setId(mockId)
        def mockIdentifier = "an identifier"
        mockDemographicsBundle.setIdentifier(new Identifier().setValue(mockIdentifier))
        def mockTimestamp = Date.from(Instant.now().minusSeconds(60))
        mockDemographicsBundle.setTimestamp(mockTimestamp)

        when:
        HapiOrderConverter.convertDemographicsToOrder(mockDemographicsResource)

        then:
        mockDemographicsResource.getId() == mockId
        mockDemographicsResource.getIdentifier().getValue() == mockIdentifier
        mockDemographicsResource.getTimestamp() == mockTimestamp
        mockDemographicsResource.getId() != mockDemographicsResource.getIdentifier().getValue()
    }

    def "the converter always changes the bundle type to message"() {
        given:
        def mockDemographicsResource = mockDemographics.getUnderlyingResource()
        mockDemographicsBundle.setType(Bundle.BundleType.COLLECTION)

        when:
        HapiOrderConverter.convertDemographicsToOrder(mockDemographicsResource)

        then:
        mockDemographicsResource.getType() == Bundle.BundleType.MESSAGE
    }

    def "the demographics correctly constructs a message header in the lab order"() {
        given:
        def mockDemographicsResource = mockDemographics.getUnderlyingResource()

        when:
        HapiOrderConverter.convertDemographicsToOrder(mockDemographicsResource)
        def messageHeader = HapiHelper.resourcesInBundle(mockDemographicsResource, MessageHeader.class).findFirst().orElse(null)

        then:
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
        given:
        def mockDemographicsResource = mockDemographics.getUnderlyingResource()

        when:
        HapiOrderConverter.convertDemographicsToOrder(mockDemographicsResource)
        def patient = HapiHelper.resourcesInBundle(mockDemographicsResource, Patient.class).findFirst().orElse(null)

        then:
        patient == mockPatient
    }

    def "the converter correctly constructs a service request in the lab order"() {
        given:
        def mockDemographicsResource = mockDemographics.getUnderlyingResource()

        when:
        HapiOrderConverter.convertDemographicsToOrder(mockDemographicsResource)
        def serviceRequest = HapiHelper.resourcesInBundle(mockDemographicsResource, ServiceRequest.class).findFirst().orElse(null)

        then:
        serviceRequest.hasId()
        serviceRequest.getCode().getCodingFirstRep().getCode() == "54089-8"
        serviceRequest.getCategoryFirstRep().getCodingFirstRep().getCode() == "108252007"
        serviceRequest.getSubject().getResource() == mockDemographicsResource.getEntry().get(1).getResource()
        serviceRequest.hasAuthoredOn()
    }

    def "the order datetime should match for bundle, service request, and provenance resources"() {
        given:
        def mockDemographicsResource = mockDemographics.getUnderlyingResource()

        when:
        HapiOrderConverter.convertDemographicsToOrder(mockDemographicsResource)
        def bundleDateTime = mockDemographicsResource.getTimestamp()
        def serviceRequest = HapiHelper.resourcesInBundle(mockDemographicsResource, ServiceRequest.class).findFirst().orElse(null)
        def provenance = HapiHelper.resourcesInBundle(mockDemographicsResource, Provenance.class).findFirst().orElse(null)
        def serviceRequestDateTime = serviceRequest.getAuthoredOn()
        def provenanceDateTime = provenance.getRecorded()

        then:
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
        HapiOrderConverter.convertToOmlOrder(mockOrder.getUnderlyingResource())

        then:
        def convertedMessageHeader =
                HapiHelper.resourcesInBundle(mockOrder.getUnderlyingResource(), MessageHeader.class).findFirst().orElse(null)

        convertedMessageHeader != null
        convertedMessageHeader.getEventCoding().getCode() == "O21"
        convertedMessageHeader.getEventCoding().getDisplay().contains("OML")
    }

    def "adds the message header to specify OML"() {
        when:
        HapiOrderConverter.convertToOmlOrder(mockOrder.getUnderlyingResource())

        then:
        def convertedMessageHeader =
                HapiHelper.resourcesInBundle(mockOrder.getUnderlyingResource(), MessageHeader.class).findFirst().orElse(null)

        convertedMessageHeader != null
        convertedMessageHeader.getEventCoding().getCode() == "O21"
        convertedMessageHeader.getEventCoding().getDisplay().contains("OML")
    }

    def "add contact section to patient resource"() {
        given:
        def patientMothersMaidenNameURL = "http://hl7.org/fhir/StructureDefinition/patient-mothersMaidenName"
        def relationshipCode = "MTH"
        def relationshipSystem = "http://terminology.hl7.org/CodeSystem/v3-RoleCode"
        def relationshipDisplay = "mother"

        def patient = fakePatientResource(true)
        def patientEntry = new Bundle.BundleEntryComponent().setResource(patient)
        def entryList = new ArrayList<Bundle.BundleEntryComponent>()
        entryList.add(patientEntry)
        mockOrderBundle.setEntry(entryList)

        when:
        HapiOrderConverter.addContactSectionToPatientResource(mockOrder.getUnderlyingResource())

        then:
        def convertedPatient = HapiHelper.resourcesInBundle(mockOrder.getUnderlyingResource(), Patient.class).findFirst().orElse(null)
        def contactSection = convertedPatient.getContact()[0]

        contactSection != null
        // Name
        def convertedPatientHumanName =
                convertedPatient.castToHumanName(convertedPatient.getExtensionByUrl(patientMothersMaidenNameURL)
                .getValue())
        def contactSectionHunanName = contactSection.getName()
        contactSectionHunanName.getText() == convertedPatientHumanName.getText()
        contactSectionHunanName.getFamily() == convertedPatientHumanName.getFamily()
        contactSectionHunanName.getGiven() == convertedPatientHumanName.getGiven()
        // Relationship
        def relationship = contactSection.getRelationship().get(0).getCoding().get(0)
        relationship.getCode() == relationshipCode
        relationship.getSystem() == relationshipSystem
        relationship.getDisplay() == relationshipDisplay
        // Telecom
        def contactTelecom = contactSection.getTelecom().get(0)
        def convertedPatientTelecom = convertedPatient.getTelecom().get(0)
        contactTelecom.getSystem() == convertedPatientTelecom.getSystem()
        contactTelecom.getValue() == convertedPatientTelecom.getValue()
        contactTelecom.getUse() == convertedPatientTelecom.getUse()
        // Address
        def contactSectionAddress = contactSection.getAddress()
        def convertedPatientAddress = convertedPatient.getAddress().get(0)
        contactSectionAddress.getLine().get(0) == convertedPatientAddress.getLine().get(0)
        contactSectionAddress.getCity() == convertedPatientAddress.getCity()
        contactSectionAddress.getPostalCode() == convertedPatientAddress.getPostalCode()
    }

    def "add etor processing tag to messageHeader resource"() {
        given:
        def expectedSystem = "http://localcodes.org/ETOR"
        def expectedCode = "ETOR"
        def expectedDisplay = "Processed by ETOR"

        def messageHeader = new MessageHeader()
        messageHeader.setId(UUID.randomUUID().toString())
        def messageHeaderEntry = new Bundle.BundleEntryComponent().setResource(messageHeader)
        mockOrderBundle.getEntry().add(1, messageHeaderEntry)
        mockOrder.getUnderlyingResource() >> mockOrderBundle

        when:
        def convertedOrderBundle = HapiOrderConverter.getInstance().addEtorProcessingTag(mockOrder).getUnderlyingResource() as Bundle

        then:
        def messageHeaders = convertedOrderBundle.getEntry().get(1).getResource() as MessageHeader
        def actualSystem = messageHeaders.getMeta().getTag()[0].getSystem()
        def actualCode = messageHeaders.getMeta().getTag()[0].getCode()
        def actualDisplay = messageHeaders.getMeta().getTag()[0].getDisplay()
        actualSystem == expectedSystem
        actualCode == expectedCode
        actualDisplay == expectedDisplay
    }


    def "no humanName section in contact"() {
        given:
        def addHumanName = false
        def patientResourceNoHumanName = fakePatientResource(addHumanName)
        def patientEntry = new Bundle.BundleEntryComponent().setResource(patientResourceNoHumanName)
        def entryList = new ArrayList<Bundle.BundleEntryComponent>()
        entryList.add(patientEntry)
        mockOrderBundle.setEntry(entryList)
        when:
        HapiOrderConverter.addContactSectionToPatientResource(mockOrderBundle)

        then:
        def convertedPatient = HapiHelper.resourcesInBundle(mockOrderBundle, Patient.class).findFirst().orElse(null)
        def contactSection = convertedPatient.getContact().first()

        !contactSection.hasName()
    }

    Patient fakePatientResource(boolean addHumanName) {

        def patient = new Patient()

        if (addHumanName) {
            def patientExtension = new Extension()
            def patientMothersMaidenNameURL = "http://hl7.org/fhir/StructureDefinition/patient-mothersMaidenName"
            patientExtension.setUrl(patientMothersMaidenNameURL)

            def humanName = new HumanName()
            humanName.setText("SADIE S SMITH")
            humanName.setFamily("SMITH")
            humanName.addGiven("SADIE")
            humanName.addGiven("S")
            patientExtension.setValue(humanName)
            patient.addExtension(patientExtension)
        }


        def telecomExtension = new Extension()
        telecomExtension.setUrl("https://reportstream.cdc.gov/fhir/StructureDefinition/text")
        telecomExtension.setValue(new StringType("(763)555-5555"))

        def telecom = new ContactPoint()
        telecom.addExtension(telecomExtension)
        telecom.setSystem(ContactPoint.ContactPointSystem.PHONE)
        telecom.setUse(ContactPoint.ContactPointUse.HOME)

        def address = new Address()
        address.setUse(Address.AddressUse.HOME)
        address.addLine("555 STATE HIGHWAY 13")
        address.setCity("DEER CREEK")
        address.setDistrict("OTTER TAIL")
        address.setState("MN")
        address.setPostalCode("56527-9657")
        address.setCountry("USA")

        patient.addTelecom(telecom)
        patient.addAddress(address)

        return patient
    }
}
