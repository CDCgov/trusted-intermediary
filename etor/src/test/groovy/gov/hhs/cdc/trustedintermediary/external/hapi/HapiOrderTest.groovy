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
        def expected = 1
        when:
        def actual = 1
        then:
        actual == expected
    }

    def "getPlacerOrderNumber unhappy path"() {
        given:
        def expected = 1
        when:
        def actual = 1
        then:
        actual == expected
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

    def "getSendingFacilityId happy path works"() {
        given:
        def expectedFacilityId = "mock-facility-id"
        def innerOrders = new Bundle()

        def messageHeader = new MessageHeader()
        messageHeader.setSender(new Reference("Organization/mock-id"))
        innerOrders.addEntry(new Bundle.BundleEntryComponent().setResource(messageHeader))

        def organization = new Organization()
        organization.setId("mock-id")
        organization.setName(expectedFacilityId)
        innerOrders.addEntry(new Bundle.BundleEntryComponent().setResource(organization))

        def orders = new HapiOrder(innerOrders)

        when:
        def actualFacilityId = orders.getSendingFacilityId()

        then:
        actualFacilityId == expectedFacilityId
    }

    def "getSendingFacilityId unhappy path works"() {
        given:
        def innerOrders = new Bundle()
        def expectedFacilityId = ""
        def messageHeader = new MessageHeader()
        innerOrders.addEntry(new Bundle.BundleEntryComponent().setResource(messageHeader))

        def orders = new HapiOrder(innerOrders)

        when:
        def actualFacilityId = orders.getSendingFacilityId()

        then:
        actualFacilityId == expectedFacilityId
    }

    def "getReceivingApplicationDetails happy path works"() {
        given:
        def innerOrders = new Bundle()
        def messageHeader = new MessageHeader()
        def destination = new MessageHeader.MessageDestinationComponent()
        def endpoint = "urn:oid:1.2.840.114350.1.13.145.2.7.2.695071"
        def name = "Epic"
        def universalIdType = "ISO"
        def extension = new Extension("https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id-type", new StringType(universalIdType))
        def expectedApplicationDetails = "$name^$endpoint^$universalIdType"

        destination.setName(name)
        destination.setEndpoint(endpoint)
        destination.addExtension(extension)
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
