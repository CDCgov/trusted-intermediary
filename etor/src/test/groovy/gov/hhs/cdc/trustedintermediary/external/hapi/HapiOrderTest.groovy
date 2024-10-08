package gov.hhs.cdc.trustedintermediary.external.hapi

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.etor.messages.MessageHdDataType
import gov.hhs.cdc.trustedintermediary.wrappers.HapiFhir
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

import javax.print.attribute.standard.Destination

class HapiOrderTest extends Specification {

    def fhirEngine = HapiFhirImplementation.getInstance()

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(HapiFhir.class, fhirEngine)
        TestApplicationContext.register(HapiMessageHelper.class, HapiMessageHelper.getInstance())
        TestApplicationContext.injectRegisteredImplementations()
    }

    def "getUnderlyingData Works"() {
        given:
        def expectedInnerOrder = new Bundle()
        def order = new HapiOrder(expectedInnerOrder)

        when:
        def actualInnerOrder = order.getUnderlyingData()

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
        def orders = setupOrderWithEmptyMessageHeader()
        def expectedPlacerOrderNumber = ""

        when:
        def actualPlacerOrderNumber = orders.getPlacerOrderNumber()

        then:
        actualPlacerOrderNumber == expectedPlacerOrderNumber
    }

    def "getSendingApplicationDetails happy path works"() {
        given:
        def nameSpaceId = "Natus"
        def universalId = "natus.health.state.mn.us"
        def universalIdType = "DNS"
        def orders = setupOrderWithSendingApplicationDetails(nameSpaceId, universalId, universalIdType)
        def expectedApplicationDetails = new MessageHdDataType(nameSpaceId, universalId, universalIdType)

        when:
        def actualApplicationDetails = orders.getSendingApplicationDetails()

        then:
        actualApplicationDetails.namespace() == expectedApplicationDetails.namespace()
        actualApplicationDetails.universalId() == expectedApplicationDetails.universalId()
        actualApplicationDetails.universalIdType() == expectedApplicationDetails.universalIdType()
    }

    def "getSendingApplicationDetails unhappy path works"() {
        given:
        def expectedApplicationDetails = new MessageHdDataType("", "", "")
        def orders = setupOrderWithEmptyMessageHeader()

        when:
        def actualApplicationDetails = orders.getSendingApplicationDetails()

        then:
        actualApplicationDetails.namespace() == expectedApplicationDetails.namespace()
        actualApplicationDetails.universalId() == expectedApplicationDetails.universalId()
        actualApplicationDetails.universalIdType() == expectedApplicationDetails.universalIdType()
    }

    def "getSendingFacilityDetails happy path works"() {
        given:
        def facilityName = "MN Public Health Lab"
        def universalId = "2.16.840.1.114222.4.1.10080"
        def universalIdType = "ISO"
        def orders = setupOrderWithSendingFacilityDetails(facilityName, universalId, universalIdType)
        def expectedFacilityDetails = new MessageHdDataType(facilityName, universalId, universalIdType)

        when:
        def actualFacilityDetails = orders.getSendingFacilityDetails()

        then:
        actualFacilityDetails.namespace() == expectedFacilityDetails.namespace()
        actualFacilityDetails.universalId() == expectedFacilityDetails.universalId()
        actualFacilityDetails.universalIdType() == expectedFacilityDetails.universalIdType()
    }

    def "getSendingFacilityDetails unhappy path works"() {
        given:
        def expectedFacilityDetails = new MessageHdDataType("", "", "")
        def orders = setupOrderWithEmptyMessageHeader()

        when:
        def actualFacilityDetails = orders.getSendingFacilityDetails()

        then:
        actualFacilityDetails.namespace() == expectedFacilityDetails.namespace()
        actualFacilityDetails.universalId() == expectedFacilityDetails.universalId()
        actualFacilityDetails.universalIdType() == expectedFacilityDetails.universalIdType()
    }

    def "getReceivingApplicationDetails happy path works"() {
        given:
        def universalId = "1.2.840.114350.1.13.145.2.7.2.695071"
        def namespaceId = "Epic"
        def universalIdType = "ISO"
        def expectedApplicationDetails = new MessageHdDataType(namespaceId, universalId, universalIdType)
        def orders = setupOrderWithReceivingApplicationDetails(namespaceId, universalId, universalIdType)

        when:
        def actualApplicationDetails = orders.getReceivingApplicationDetails()

        then:
        actualApplicationDetails.namespace() == expectedApplicationDetails.namespace()
        actualApplicationDetails.universalId() == expectedApplicationDetails.universalId()
        actualApplicationDetails.universalIdType() == expectedApplicationDetails.universalIdType()
    }

    def "getReceivingApplicationDetails unhappy path works"() {
        given:
        def orders = setupOrderWithEmptyMessageHeader()
        def expectedApplicationDetails = new MessageHdDataType("", "", "")

        when:
        def actualApplicationDetails = orders.getReceivingApplicationDetails()

        then:
        actualApplicationDetails.namespace() == expectedApplicationDetails.namespace()
        actualApplicationDetails.universalId() == expectedApplicationDetails.universalId()
        actualApplicationDetails.universalIdType() == expectedApplicationDetails.universalIdType()
    }

    def "getReceivingFacilityDetails happy path works"() {
        given:
        def facilityName = "Central Hospital"
        def universalId = "2.16.840.1.113883.3.4.5"
        def universalIdType = "ISO"
        def expectedFacilityDetails = new MessageHdDataType(facilityName, universalId, universalIdType)
        def orders = setupOrderWithReceivingFacilityDetails(facilityName, universalId, universalIdType)

        when:
        def actualFacilityDetails = orders.getReceivingFacilityDetails()

        then:
        actualFacilityDetails.namespace() == expectedFacilityDetails.namespace()
        actualFacilityDetails.universalId() == expectedFacilityDetails.universalId()
        actualFacilityDetails.universalIdType() == expectedFacilityDetails.universalIdType()
    }

    def "getReceivingFacilityDetails unhappy path works"() {
        given:
        def expectedFacilityDetails = new MessageHdDataType("", "", "")
        def orders = setupOrderWithEmptyMessageHeader()

        when:
        def actualFacilityDetails = orders.getReceivingFacilityDetails()

        then:
        actualFacilityDetails.namespace() == expectedFacilityDetails.namespace()
        actualFacilityDetails.universalId() == expectedFacilityDetails.universalId()
        actualFacilityDetails.universalIdType() == expectedFacilityDetails.universalIdType()
    }

    def "extractMessageHdDataType works" () {
        given:
        def namespace = "Central Hospital"
        def universalId = "2.16.842.1.113883.4.5"
        def universalIdType = "ISO"
        def expectedDetails = new MessageHdDataType(namespace, universalId, universalIdType)
        def hapiResult = new HapiResult(null)

        when:
        def actualDetails = hapiResult.extractMessageHdDataType(
                {namespace},
                {universalId},
                {universalIdType})

        then:
        actualDetails.namespace() == expectedDetails.namespace()
        actualDetails.universalId() == expectedDetails.universalId()
        actualDetails.universalIdType() == expectedDetails.universalIdType()
    }

    protected HapiOrder setupOrderWithSendingApplicationDetails(String nameSpaceId, String universalId, String universalIdType) {
        def innerOrders = new Bundle()
        def messageHeader = new MessageHeader()
        def endpoint = "urn:dns:natus.health.state.mn.us"
        messageHeader.setSource(new MessageHeader.MessageSourceComponent(new UrlType(endpoint)))
        def nameSpaceIdExtension = new Extension("https://reportstream.cdc.gov/fhir/StructureDefinition/namespace-id", new StringType(nameSpaceId))
        messageHeader.getSource().addExtension(nameSpaceIdExtension)
        def universalIdExtension = new Extension("https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id", new StringType(universalId))
        messageHeader.getSource().addExtension(universalIdExtension)
        def universalIdTypeExtension = new Extension("https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id-type", new StringType(universalIdType))
        messageHeader.getSource().addExtension(universalIdTypeExtension)
        innerOrders.addEntry(new Bundle.BundleEntryComponent().setResource(messageHeader))
        return new HapiOrder(innerOrders)
    }

    protected  HapiOrder setupOrderWithSendingFacilityDetails(String facilityName, String universalId, String universalIdType) {
        def innerOrders = new Bundle()
        def messageHeader = new MessageHeader()
        def orgReference = "Organization/1708034743302204787.82104dfb-e854-47de-b7ce-19a2b71e61db"
        messageHeader.setSender(new Reference(orgReference))
        innerOrders.addEntry(new Bundle.BundleEntryComponent().setResource(messageHeader))
        def organization = new Organization()
        organization.setId("1708034743302204787.82104dfb-e854-47de-b7ce-19a2b71e61db")
        // facility name
        def facilityIdentifier = new Identifier()
        def facilityNameExtension = new Extension("https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2Field", new StringType("HD.1"))
        facilityIdentifier.addExtension(facilityNameExtension)
        facilityIdentifier.setValue(facilityName)
        // universal id
        def universalIdIdentifier = new Identifier()
        def universalIdExtension = new Extension("https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2Field", new StringType("HD.2,HD.3"))
        universalIdIdentifier.addExtension(universalIdExtension)
        universalIdIdentifier.setValue(universalId)
        // Type
        def typeConcept = new CodeableConcept()
        def coding = new Coding("http://terminology.hl7.org/CodeSystem/v2-0301", universalIdType, null)
        typeConcept.addCoding(coding)
        universalIdIdentifier.setType(typeConcept)

        organization.addIdentifier(facilityIdentifier)
        organization.addIdentifier(universalIdIdentifier)
        innerOrders.addEntry(new Bundle.BundleEntryComponent().setResource(organization))

        // Convert orders to json so the reference is added as part of the bundle so we can use .resolve()
        // as part of the fhir path.
        def jsonOrders = fhirEngine.parseResource(fhirEngine.encodeResourceToJson(innerOrders), Bundle)
        return new HapiOrder(jsonOrders)
    }

    protected HapiOrder setupOrderWithReceivingApplicationDetails(String namespaceId, String universalId, String universalIdType) {
        def innerOrders = new Bundle()
        def messageHeader = new MessageHeader()
        def destination = new MessageHeader.MessageDestinationComponent()
        def universalIdExtension = new Extension("https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id", new StringType(universalId))
        def universalIdTypeExtension = new Extension("https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id-type", new StringType(universalIdType))

        destination.setName(namespaceId)
        destination.addExtension(universalIdExtension)
        destination.addExtension(universalIdTypeExtension)
        messageHeader.setDestination([destination])
        innerOrders.addEntry(new Bundle.BundleEntryComponent().setResource(messageHeader))
        return new HapiOrder(innerOrders)
    }

    protected HapiOrder setupOrderWithReceivingFacilityDetails(String facilityName, String universalId, String universalIdType) {
        def innerOrders = new Bundle()
        def messageHeader = new MessageHeader()
        def organizationReference = "Organization/1708034743312390878.b61e734a-4d65-4e25-b423-cdb19018d84a"
        def destination = new MessageHeader.MessageDestinationComponent()
        destination.setReceiver(new Reference(organizationReference))
        messageHeader.addDestination(destination)
        innerOrders.addEntry(new Bundle.BundleEntryComponent().setResource(messageHeader))
        def organization = new Organization()
        organization.setId("1708034743312390878.b61e734a-4d65-4e25-b423-cdb19018d84a")
        // Facility
        def identifierFacilityName = new Identifier()
        def extensionFacilityName = new Extension("https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2Field", new StringType("HD.1"))
        identifierFacilityName.addExtension(extensionFacilityName)
        identifierFacilityName.setValue(facilityName)
        // Universal ID
        def identifierUniversalId = new Identifier()
        def extensionUniversalId = new Extension("https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2Field", new StringType("HD.2,HD.3"))
        identifierUniversalId.addExtension(extensionUniversalId)
        identifierUniversalId.setValue(universalId)
        // Universal ID type
        def coding = new Coding("http://terminology.hl7.org/CodeSystem/v2-0203", universalIdType, null)
        def typeCodeableConcept = new CodeableConcept()
        typeCodeableConcept.addCoding(coding)
        identifierUniversalId.setType(typeCodeableConcept)

        organization.addIdentifier(identifierFacilityName)
        organization.addIdentifier(identifierUniversalId)
        innerOrders.addEntry(new Bundle.BundleEntryComponent().setResource(organization))

        // Convert orders to json so the reference is added as part of the bundle so we can use .resolve()
        // as part of the fhir path.
        def jsonOrders = fhirEngine.parseResource(fhirEngine.encodeResourceToJson(innerOrders), Bundle)
        return new HapiOrder(jsonOrders)
    }

    protected HapiOrder setupOrderWithEmptyMessageHeader() {
        def innerOrders = new Bundle()
        MessageHeader messageHeader = new MessageHeader()
        innerOrders.addEntry(new Bundle.BundleEntryComponent().setResource(messageHeader))
        return new HapiOrder(innerOrders)
    }
}
