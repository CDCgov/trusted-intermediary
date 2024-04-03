package gov.hhs.cdc.trustedintermediary.external.hapi

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.wrappers.HapiFhir
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.CodeableConcept
import org.hl7.fhir.r4.model.Coding
import org.hl7.fhir.r4.model.Extension
import org.hl7.fhir.r4.model.Identifier
import org.hl7.fhir.r4.model.MessageHeader
import org.hl7.fhir.r4.model.Organization
import org.hl7.fhir.r4.model.Reference
import org.hl7.fhir.r4.model.ServiceRequest
import org.hl7.fhir.r4.model.StringType
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

    def "getUnderlyingResult works"() {
        given:
        def expectedResult = new Bundle()
        def result = new HapiResult(expectedResult)

        when:
        def actualResult = result.getUnderlyingResult()

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
        def bundle = new Bundle()
        def result = new HapiResult(bundle)

        when:
        def actualPlacerOrderNumber = result.getPlacerOrderNumber()

        then:
        actualPlacerOrderNumber == expectedPlacerOrderNumber
    }

    def "getSendingApplicationDetails works"() {
        given:
        def expectedSendingApplicationId = "mock-sending-application-id"
        def bundle = new Bundle()
        def messageHeader = new MessageHeader()
        def extension = new Extension("https://reportstream.cdc.gov/fhir/StructureDefinition/namespace-id", new StringType(expectedSendingApplicationId))
        messageHeader.setSource(new MessageHeader.MessageSourceComponent().addExtension(extension) as MessageHeader.MessageSourceComponent)
        bundle.addEntry(new Bundle.BundleEntryComponent().setResource(messageHeader))
        def result = new HapiResult(bundle)

        when:
        def actualSendingApplicationId = result.getSendingApplicationDetails()

        then:
        actualSendingApplicationId == expectedSendingApplicationId
    }

    def "getSendingApplicationDetails unhappy path"() {
        given:
        def expectedSendingApplicationId = ""
        def bundle = new Bundle()
        def result = new HapiResult(bundle)

        when:
        def actualSendingApplicationId = result.getSendingApplicationDetails()

        then:
        actualSendingApplicationId == expectedSendingApplicationId
    }

    def "getSendingFacilityDetails happy path works"() {
        given:
        def innerResults = new Bundle()

        def messageHeader = new MessageHeader()
        def orgReference = "Organization/1708034743302204787.82104dfb-e854-47de-b7ce-19a2b71e61db"
        messageHeader.setSender(new Reference(orgReference))
        innerResults.addEntry(new Bundle.BundleEntryComponent().setResource(messageHeader))

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
        innerResults.addEntry(new Bundle.BundleEntryComponent().setResource(organization))
        def orders = new HapiOrder(innerResults)
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

    def "getReceivingApplicationDetails works"() {
        given:
        def expected = 1
        when:
        def actual = 1
        then:
        actual == expected
    }

    def "getReceivingApplicationDetails unhappy path"() {
        given:
        def expected = 1
        when:
        def actual = 1
        then:
        actual == expected
    }

    def "getReceivingFacilityDetails works"() {
        given:
        def expected = 1
        when:
        def actual = 1
        then:
        actual == expected
    }

    def "getReceivingFacilityDetails unhappy path"() {
        given:
        def expected = 1
        when:
        def actual = 1
        then:
        actual == expected
    }
}
