package gov.hhs.cdc.trustedintermediary.external.hapi

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.etor.messages.MessageHdDataType
import gov.hhs.cdc.trustedintermediary.wrappers.HapiFhir
import org.hl7.fhir.r4.model.*
import spock.lang.Specification

class HapiResultTest extends Specification {

    def fhirEngine = HapiFhirImplementation.getInstance()

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(HapiFhir.class, fhirEngine)
        TestApplicationContext.register(HapiMessageHelper.class, HapiMessageHelper.getInstance())
        TestApplicationContext.injectRegisteredImplementations()
    }

    def "getUnderlyingElement works"() {
        given:
        def expectedResult = new Bundle()
        def result = new HapiResult(expectedResult)

        when:
        def actualResult = result.getUnderlyingElement()

        then:
        actualResult == expectedResult
    }

    def "getFhirResourceId works"() {
        given:
        def expectId = "fhirResourceId"
        def innerResult = new Bundle()
        innerResult.setId(expectId)

        when:
        def actualResult = new HapiResult(innerResult)

        then:
        actualResult.getFhirResourceId() == expectId
    }

    def "getPlacerOrderNumber works"() {
        given:
        def expectedPlacerOrderNumber = "mock-placer-order-number"
        def bundle = new Bundle()
        def serviceRequest = new ServiceRequest().addIdentifier(new Identifier()
                .setValue(expectedPlacerOrderNumber)
                .setType(new CodeableConcept().addCoding(new Coding("http://terminology.hl7.org/CodeSystem/v2-0203", "PLAC", "Placer Identifier"))))
        bundle.addEntry(new Bundle.BundleEntryComponent().setResource(serviceRequest))
        def result = new HapiResult(bundle)

        when:
        def actualPlacerOrderNumber = result.getPlacerOrderNumber()

        then:
        actualPlacerOrderNumber == expectedPlacerOrderNumber
    }

    def "getPlacerOrderNumber unhappy path"() {
        given:
        def expectedPlacerOrderNumber = ""
        def result = setupOrderWithEmptyMessageHeader()

        when:
        def actualPlacerOrderNumber = result.getPlacerOrderNumber()

        then:
        actualPlacerOrderNumber == expectedPlacerOrderNumber
    }

    def "getSendingApplicationDetails works"() {
        given:
        def nameSpaceId = "Natus"
        def universalId = "natus.health.state.mn.us"
        def universalIdType = "DNS"
        def expectedApplicationDetails = new MessageHdDataType(nameSpaceId, universalId, universalIdType)
        def orders = setupOrderWithSendingApplicationDetails(nameSpaceId, universalId, universalIdType)

        when:
        def actualApplicationDetails = orders.getSendingApplicationDetails()

        then:
        actualApplicationDetails.namespace() == expectedApplicationDetails.namespace()
        actualApplicationDetails.universalId() == expectedApplicationDetails.universalId()
        actualApplicationDetails.universalIdType() == expectedApplicationDetails.universalIdType()
    }

    def "getSendingApplicationDetails unhappy path"() {
        given:
        def expectedApplicationDetails = new MessageHdDataType("", "", "")
        def result = setupOrderWithEmptyMessageHeader()

        when:
        def actualApplicationDetails = result.getSendingApplicationDetails()

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
        def results = setupOrderWithSendingFacilityDetails(facilityName, universalId, universalIdType)
        def expectedFacilityDetails = new MessageHdDataType(facilityName, universalId, universalIdType)

        when:
        def actualFacilityDetails = results.getSendingFacilityDetails()

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

    def "getReceivingApplicationDetails works"() {
        given:
        def innerResults = new Bundle()
        def messageHeader = new MessageHeader()
        def destination = new MessageHeader.MessageDestinationComponent()
        def universalId = "1.2.840.114350.1.13.145.2.7.2.695071"
        def name = "Epic"
        def universalIdType = "ISO"
        def universalIdExtension = new Extension("https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id", new StringType(universalId))
        def universalIdTypeExtension = new Extension("https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id-type", new StringType(universalIdType))
        def expectedApplicationDetails = new MessageHdDataType(name, universalId, universalIdType)

        destination.setName(name)
        destination.addExtension(universalIdExtension)
        destination.addExtension(universalIdTypeExtension)
        messageHeader.setDestination([destination])
        innerResults.addEntry(new Bundle.BundleEntryComponent().setResource(messageHeader))
        def results = new HapiResult(innerResults)

        when:
        def actualApplicationDetails = results.getReceivingApplicationDetails()

        then:
        actualApplicationDetails.namespace() == expectedApplicationDetails.namespace()
        actualApplicationDetails.universalId() == expectedApplicationDetails.universalId()
        actualApplicationDetails.universalIdType() == expectedApplicationDetails.universalIdType()
    }

    def "getReceivingApplicationDetails unhappy path"() {
        given:
        def results = setupOrderWithEmptyMessageHeader()
        def expectedApplicationDetails = new MessageHdDataType("", "", "")

        when:
        def actualApplicationDetails = results.getReceivingApplicationDetails()

        then:
        actualApplicationDetails.namespace() == expectedApplicationDetails.namespace()
        actualApplicationDetails.universalId() == expectedApplicationDetails.universalId()
        actualApplicationDetails.universalIdType() == expectedApplicationDetails.universalIdType()
    }

    def "getReceivingFacilityDetails works"() {
        given:
        def innerResults = new Bundle()
        def messageHeader = new MessageHeader()
        def destination = new MessageHeader.MessageDestinationComponent(new UrlType("urn:oid:1.2.840.114350.1.13.145.2.7.2.695071"))
        def orgReference = "Organization/1708034743302204787.82104dfb-e854-47de-b7ce-19a2b71e61db"
        destination.setReceiver(new Reference(orgReference))
        messageHeader.setDestination(Arrays.asList(destination))
        innerResults.addEntry(new Bundle.BundleEntryComponent().setResource(messageHeader))

        def organization = new Organization()
        organization.setId("1708034743302204787.82104dfb-e854-47de-b7ce-19a2b71e61db")

        // facility name
        def facilityIdentifier = new Identifier()
        def facilityName = "Samtracare"
        def facilityNameExtension = new Extension("https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2Field", new StringType("HD.1"))
        facilityIdentifier.addExtension(facilityNameExtension)
        facilityIdentifier.setValue(facilityName)

        // universal id
        def universalIdIdentifier = new Identifier()
        def universalIdIdentifierValue = "Samtracare.com"
        def universalIdExtension = new Extension("https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2Field", new StringType("HD.2,HD.3"))
        universalIdIdentifier.addExtension(universalIdExtension)
        universalIdIdentifier.setValue(universalIdIdentifierValue)

        // Type
        def typeConcept = new CodeableConcept()
        def theCode = "DNS"
        def coding = new Coding("http://terminology.hl7.org/CodeSystem/v2-0301", theCode, null)
        typeConcept.addCoding(coding)
        universalIdIdentifier.setType(typeConcept)

        organization.addIdentifier(facilityIdentifier)
        organization.addIdentifier(universalIdIdentifier)
        innerResults.addEntry(new Bundle.BundleEntryComponent().setResource(organization))
        def parsedResults = fhirEngine.parseResource(fhirEngine.encodeResourceToJson(innerResults), Bundle)
        def results = new HapiResult(parsedResults)
        def expectedFacilityDetails = new MessageHdDataType(facilityName, universalIdIdentifierValue, theCode)

        when:
        def actualFacilityDetails = results.getReceivingFacilityDetails()

        then:
        actualFacilityDetails.namespace() == expectedFacilityDetails.namespace()
        actualFacilityDetails.universalId() == expectedFacilityDetails.universalId()
        actualFacilityDetails.universalIdType() == expectedFacilityDetails.universalIdType()
    }

    def "getReceivingFacilityDetails unhappy path"() {
        given:
        def results = setupOrderWithEmptyMessageHeader()
        def expectedApplicationDetails = new MessageHdDataType("", "", "")

        when:
        def actualApplicationDetails = results.getReceivingFacilityDetails()

        then:
        actualApplicationDetails.namespace() == expectedApplicationDetails.namespace()
        actualApplicationDetails.universalId() == expectedApplicationDetails.universalId()
        actualApplicationDetails.universalIdType() == expectedApplicationDetails.universalIdType()
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
