package gov.hhs.cdc.trustedintermediary.external.hapi

import gov.hhs.cdc.trustedintermediary.OrderMock
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.hl7fhir.FhirValuesHelper
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Coding
import org.hl7.fhir.r4.model.MessageHeader
import org.hl7.fhir.r4.model.Meta
import org.hl7.fhir.r4.model.Patient
import spock.lang.Specification

class HapiMessageConverterHelperTest extends Specification {

    Bundle mockBundle
    Bundle mockOrderBundle
    Patient mockPatient
    OrderMock<Bundle> mockOrder

    def expectedSystem = "http://localcodes.org/ETOR"
    def expectedCode = "ETOR"
    def expectedDisplay = "Processed by ETOR"

    def setup() {
        mockBundle = null
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.injectRegisteredImplementations()

        mockPatient = new Patient()
        mockOrderBundle = new Bundle().addEntry(new Bundle.BundleEntryComponent().setResource(mockPatient))
        mockBundle = new Bundle().addEntry(new Bundle.BundleEntryComponent().setResource(mockPatient))
        mockOrder = new OrderMock("fhirResourceId", "patientId", mockOrderBundle, null, null, null, null, null)
    }

    def "addEtorTag adds the ETOR message header tag to any Bundle"() {
        given:
        def messageHeader = new MessageHeader()
        messageHeader.setId(UUID.randomUUID().toString())
        def messageHeaderEntry = new Bundle.BundleEntryComponent().setResource(messageHeader)
        mockBundle.getEntry().add(messageHeaderEntry)

        when:
        HapiMessageConverterHelper.addMetaTag(mockBundle, expectedSystem, expectedCode, expectedDisplay)

        then:
        def messageHeaders = mockBundle.getEntry().get(1).getResource() as MessageHeader
        def actualMessageTag = messageHeaders.getMeta().getTag()[0]

        actualMessageTag.getSystem() == expectedSystem
        actualMessageTag.getCode() == expectedCode
        actualMessageTag.getDisplay() == expectedDisplay
    }

    def "addEtorTag adds the ETOR message header tag to any Bundle when message header is missing"() {
        when:
        HapiMessageConverterHelper.addMetaTag(mockBundle, expectedSystem, expectedCode, expectedDisplay)

        then:
        def messageHeaders = mockBundle.getEntry().get(1).getResource() as MessageHeader
        def actualMessageTag = messageHeaders.getMeta().getTag()[0]

        actualMessageTag.getSystem() == expectedSystem
        actualMessageTag.getCode() == expectedCode
        actualMessageTag.getDisplay() == expectedDisplay
    }

    def "addEtorTag adds the ETOR message header tag to any Bundle with existing Meta tag"() {
        given:
        def firstExpectedSystem = "a system"
        def firstExpectedCode = "test"
        def firstExpectedDisplay = "Processed by tests"

        def messageHeader = new MessageHeader()
        messageHeader.setId(UUID.randomUUID().toString())
        def meta = new Meta()
        meta.addTag(new Coding(firstExpectedSystem, firstExpectedCode, firstExpectedDisplay))
        messageHeader.setMeta(meta)
        def messageHeaderEntry = new Bundle.BundleEntryComponent().setResource(messageHeader)
        mockBundle.getEntry().add(messageHeaderEntry)

        when:
        HapiMessageConverterHelper.addMetaTag(mockBundle, expectedSystem, expectedCode, expectedDisplay)

        then:
        def messageHeaders = mockBundle.getEntry().get(1).getResource() as MessageHeader
        def firstActualMessageTag = messageHeaders.getMeta().getTag()[0]
        def secondActualMessageTag = messageHeaders.getMeta().getTag()[1]

        firstActualMessageTag.getSystem() == firstExpectedSystem
        firstActualMessageTag.getCode() == firstExpectedCode
        firstActualMessageTag.getDisplay() == firstExpectedDisplay

        secondActualMessageTag.getSystem() == expectedSystem
        secondActualMessageTag.getCode() == expectedCode
        secondActualMessageTag.getDisplay() == expectedDisplay
    }

    def "addEtorTag adds the ETOR header tag only once"() {
        given:
        def etorTag = new Coding(expectedSystem, expectedCode, expectedDisplay)

        def messageHeader = new MessageHeader()
        messageHeader.setId(UUID.randomUUID().toString())
        def messageHeaderEntry = new Bundle.BundleEntryComponent().setResource(messageHeader)
        mockBundle.getEntry().add(messageHeaderEntry)

        when:
        HapiMessageConverterHelper.addMetaTag(mockBundle, expectedSystem, expectedCode, expectedDisplay)
        messageHeader.getMeta().getTag().findAll {it.system == etorTag.system}.size() == 1
        HapiMessageConverterHelper.addMetaTag(mockBundle, expectedSystem, expectedCode, expectedDisplay)

        then:
        messageHeader.getMeta().getTag().findAll {it.system == etorTag.system}.size() == 1
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
        HapiMessageConverterHelper.setMessageTypeCoding(mockOrder.getUnderlyingResource(), FhirValuesHelper.OML_CODING)

        then:
        def convertedMessageHeader =
                HapiHelper.resourcesInBundle(mockOrder.getUnderlyingResource(), MessageHeader.class).findFirst().orElse(null)

        convertedMessageHeader != null
        convertedMessageHeader.getEventCoding().getCode() == "O21"
        convertedMessageHeader.getEventCoding().getDisplay().contains("OML")
    }

    def "adds the message header to specify OML"() {
        when:
        HapiMessageConverterHelper.setMessageTypeCoding(mockOrder.getUnderlyingResource(), FhirValuesHelper.OML_CODING)

        then:
        def convertedMessageHeader =
                HapiHelper.resourcesInBundle(mockOrder.getUnderlyingResource(), MessageHeader.class).findFirst().orElse(null)

        convertedMessageHeader != null
        convertedMessageHeader.getEventCoding().getCode() == "O21"
        convertedMessageHeader.getEventCoding().getDisplay().contains("OML")
    }
}
