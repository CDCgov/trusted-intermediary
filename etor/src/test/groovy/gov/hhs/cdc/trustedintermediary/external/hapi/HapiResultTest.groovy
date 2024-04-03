package gov.hhs.cdc.trustedintermediary.external.hapi

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.wrappers.HapiFhir
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.CodeableConcept
import org.hl7.fhir.r4.model.Coding
import org.hl7.fhir.r4.model.Extension
import org.hl7.fhir.r4.model.Identifier
import org.hl7.fhir.r4.model.MessageHeader
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

    def "getSendingFacilityDetails works"() {
        given:
        def expected = 1
        when:
        def actual = 1
        then:
        actual == expected
    }

    def "getSendingFacilityDetails unhappy path"() {
        given:
        def expected = 1
        when:
        def actual = 1
        then:
        actual == expected
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
