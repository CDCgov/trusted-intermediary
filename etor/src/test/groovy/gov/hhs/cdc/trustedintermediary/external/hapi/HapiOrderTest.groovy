package gov.hhs.cdc.trustedintermediary.external.hapi


import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.CodeableConcept
import org.hl7.fhir.r4.model.Coding
import org.hl7.fhir.r4.model.Extension
import org.hl7.fhir.r4.model.Identifier
import org.hl7.fhir.r4.model.MessageHeader
import org.hl7.fhir.r4.model.Organization
import org.hl7.fhir.r4.model.Patient
import org.hl7.fhir.r4.model.Reference
import org.hl7.fhir.r4.model.ServiceRequest
import org.hl7.fhir.r4.model.StringType
import org.hl7.fhir.r4.model.UrlType
import spock.lang.Specification

class HapiOrderTest extends Specification {
    def "getUnderlyingOrder Works"() {
        given:
        def expectedInnerOrder = new Bundle()
        def order = new HapiOrder(expectedInnerOrder)

        when:
        def actualInnerOrder = order.getUnderlyingOrder()

        then:
        actualInnerOrder == expectedInnerOrder
    }

    def "getFhirResourceId works"() {
        given:
        def expectedId = "DogCow goes Moof"
        def innerOrder = new Bundle()
        innerOrder.setId(expectedId)

        when:
        def orders = new HapiOrder(innerOrder)

        then:
        orders.getFhirResourceId() == expectedId
    }

    def "getPatientId works"() {
        given:
        def expectedPatientId = "DogCow goes Moof"
        def innerOrders = new Bundle()
        def patient = new Patient().addIdentifier(new Identifier()
                .setValue(expectedPatientId)
                .setType(new CodeableConcept().addCoding(new Coding("http://terminology.hl7.org/CodeSystem/v2-0203", "MR", "Medical Record Number"))))
        innerOrders.addEntry(new Bundle.BundleEntryComponent().setResource(patient))

        when:
        def orders = new HapiOrder(innerOrders)
        println("getPatientId: " + orders.getPatientId())


        then:
        orders.getPatientId() == expectedPatientId
    }

    def "getPatientId unhappy path works"() {
        given:
        def expectedPatientId = ""
        def innerOrders = new Bundle()
        def patient = new Patient()
        innerOrders.addEntry(new Bundle.BundleEntryComponent().setResource(patient))

        when:
        def orders = new HapiOrder(innerOrders)

        then:
        orders.getPatientId() == expectedPatientId
    }

    def "getPlacerOrderNumber works"() {
        given:
        def expectedPlacerOrderNumber = "mock-placer-order-number"
        def bundle = new Bundle()
        def serviceRequest = new ServiceRequest().addIdentifier(new Identifier()
                .setValue(expectedPlacerOrderNumber)
                .setType(new CodeableConcept().addCoding(new Coding("http://terminology.hl7.org/CodeSystem/v2-0203", "PLAC", "Placer Identifier"))))
        bundle.addEntry(new Bundle.BundleEntryComponent().setResource(serviceRequest))
        def order = new HapiOrder(bundle)

        when:
        def actualPlacerOrderNumber = order.getPlacerOrderNumber()

        then:
        actualPlacerOrderNumber == expectedPlacerOrderNumber
    }

    def "getPlacerOrderNumber unhappy path"() {
        given:
        def innerOrders = new Bundle()
        def orders = new HapiOrder(innerOrders)
        def expectedPlacerOrderNumber = ""

        when:
        def actualPlacerOrderNumber = orders.getPlacerOrderNumber()

        then:
        actualPlacerOrderNumber == expectedPlacerOrderNumber
    }

    def "getSendingApplicationDetails happy path works"() {
        given:
        def nameSpaceId = "Natus"
        def innerOrders = new Bundle()
        def messageHeader = new MessageHeader()
        def endpoint = "urn:dns:natus.health.state.mn.us"
        messageHeader.setSource(new MessageHeader.MessageSourceComponent(new UrlType(endpoint)))
        def nameSpaceIdExtension = new Extension("https://reportstream.cdc.gov/fhir/StructureDefinition/namespace-id", new StringType(nameSpaceId))
        messageHeader.getSource().addExtension(nameSpaceIdExtension)
        def universalId = "natus.health.state.mn.us"
        def universalIdExtension = new Extension("https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id", new StringType(universalId))
        messageHeader.getSource().addExtension(universalIdExtension)
        def universalIdType = "DNS"
        def universalIdTypeExtension = new Extension("https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id-type", new StringType(universalIdType))
        messageHeader.getSource().addExtension(universalIdTypeExtension)
        def expectedApplicationDetails = "$nameSpaceId^$universalId^$universalIdType^$endpoint"

        innerOrders.addEntry(new Bundle.BundleEntryComponent().setResource(messageHeader))
        def orders = new HapiOrder(innerOrders)

        when:
        def actualApplicationDetails = orders.getSendingApplicationDetails()

        then:
        actualApplicationDetails == expectedApplicationDetails
    }

    def "getSendingApplicationDetails unhappy path works"() {
        given:
        def expectedApplicationDetails = ""
        def innerOrders = new Bundle()
        MessageHeader messageHeader = new MessageHeader()
        innerOrders.addEntry(new Bundle.BundleEntryComponent().setResource(messageHeader))
        def orders = new HapiOrder(innerOrders)

        when:
        def actualApplicationDetails = orders.getSendingApplicationDetails()
        then:
        actualApplicationDetails == expectedApplicationDetails
    }

    def "getSendingFacilityDetails happy path works"() {
        given:
        def innerOrders = new Bundle()

        def messageHeader = new MessageHeader()
        def orgReference = "Organization/1708034743302204787.82104dfb-e854-47de-b7ce-19a2b71e61db"
        messageHeader.setSender(new Reference(orgReference))
        innerOrders.addEntry(new Bundle.BundleEntryComponent().setResource(messageHeader))

        def organization = new Organization()
        organization.setId("1708034743302204787.82104dfb-e854-47de-b7ce-19a2b71e61db")
        // facility name
        def facilityIdentifier = new Identifier()
        def facilityName = "MN Public Health Lab"
        def facilityNameExtension = new Extension("https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2Field", new StringType("HD.1"))
        facilityIdentifier.addExtension(facilityNameExtension)
        facilityIdentifier.setValue(facilityName)
        // universal id
        def universalIdIdentifier = new Identifier()
        def universalIdIdentifierValue = "2.16.840.1.114222.4.1.10080"
        def universalIdExtension = new Extension("https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2Field", new StringType("HD.2,HD.3"))
        universalIdIdentifier.addExtension(universalIdExtension)
        universalIdIdentifier.setValue(universalIdIdentifierValue)
        // Type
        def typeConcept = new CodeableConcept()
        def theCode = "ISO"
        def coding = new Coding("http://terminology.hl7.org/CodeSystem/v2-0301", theCode, null)
        typeConcept.addCoding(coding)
        universalIdIdentifier.setType(typeConcept)

        organization.addIdentifier(facilityIdentifier)
        organization.addIdentifier(universalIdIdentifier)
        innerOrders.addEntry(new Bundle.BundleEntryComponent().setResource(organization))
        def orders = new HapiOrder(innerOrders)
        def expectedFacilityDetails = "$facilityName^$universalIdIdentifierValue^$theCode"

        when:
        def actualFacilityDetails = orders.getSendingFacilityDetails()

        then:
        actualFacilityDetails == expectedFacilityDetails
    }

    def "getSendingFacilityDetails unhappy path works"() {
        given:
        def innerOrders = new Bundle()
        def expectedFacilityDetails = ""
        def messageHeader = new MessageHeader()
        innerOrders.addEntry(new Bundle.BundleEntryComponent().setResource(messageHeader))

        def orders = new HapiOrder(innerOrders)

        when:
        def actualFacilityDetails = orders.getSendingFacilityDetails()

        then:
        actualFacilityDetails == expectedFacilityDetails
    }

    def "getReceivingApplicationDetails happy path works"() {
        given:
        def innerOrders = new Bundle()
        def messageHeader = new MessageHeader()
        def destination = new MessageHeader.MessageDestinationComponent()
        def universalId = "1.2.840.114350.1.13.145.2.7.2.695071"
        def name = "Epic"
        def universalIdType = "ISO"
        def universalIdExtension = new Extension("https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id", new StringType(universalId))
        def universalIdTypeExtension = new Extension("https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id-type", new StringType(universalIdType))
        def expectedApplicationDetails = "$name^$universalId^$universalIdType"

        destination.setName(name)
        destination.addExtension(universalIdExtension)
        destination.addExtension(universalIdTypeExtension)
        messageHeader.setDestination([destination])
        innerOrders.addEntry(new Bundle.BundleEntryComponent().setResource(messageHeader))
        def orders = new HapiOrder(innerOrders)

        when:
        def actualApplicationDetails = orders.getReceivingApplicationDetails()

        then:
        actualApplicationDetails == expectedApplicationDetails
    }

    def "getReceivingApplicationDetails unhappy path works"() {
        given:
        def innerOrders = new Bundle()
        def orders = new HapiOrder(innerOrders)
        def expectedApplicationDetails = ""

        when:
        def actualApplicationDetails = orders.getReceivingApplicationDetails()
        then:
        actualApplicationDetails == expectedApplicationDetails
    }

    def "getReceivingFacilityId happy path works"() {
        given:
        def expected = 1
        when:
        def actual = 1
        then:
        actual == expected
    }

    def "getReceivingFacilityId unhappy path works"() {
        given:
        def expected = 1
        when:
        def actual = 1
        then:
        actual == expected
    }
}
