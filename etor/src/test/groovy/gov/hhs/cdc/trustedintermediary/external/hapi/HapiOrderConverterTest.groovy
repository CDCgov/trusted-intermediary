package gov.hhs.cdc.trustedintermediary.external.hapi

import gov.hhs.cdc.trustedintermediary.OrderMock
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext

import org.hl7.fhir.r4.model.Address
import org.hl7.fhir.r4.model.ContactPoint
import org.hl7.fhir.r4.model.Extension
import org.hl7.fhir.r4.model.HumanName
import org.hl7.fhir.r4.model.StringType

import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Coding
import org.hl7.fhir.r4.model.MessageHeader
import org.hl7.fhir.r4.model.Patient
import spock.lang.Specification

class HapiOrderConverterTest extends Specification {

    Patient mockPatient
    Bundle mockOrderBundle
    OrderMock<Bundle> mockOrder

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(HapiMessageConverterHelper, HapiMessageConverterHelper.getInstance())
        TestApplicationContext.register(HapiMessageHelper, HapiMessageHelper.getInstance())
        TestApplicationContext.injectRegisteredImplementations()

        mockPatient = new Patient()
        mockOrderBundle = new Bundle().addEntry(new Bundle.BundleEntryComponent().setResource(mockPatient))
        mockOrder = new OrderMock("fhirResourceId", "patientId", mockOrderBundle, null, null, null, null, null)
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
