package gov.hhs.cdc.trustedintermediary.external.hapi


import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import org.hl7.fhir.r4.model.*
import spock.lang.Specification

class HapiMessageConverterHelperTest extends Specification {

    Bundle mockBundle
    Patient mockPatient

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(HapiMessageConverterHelper, HapiMessageConverterHelper.getInstance())
        TestApplicationContext.injectRegisteredImplementations()

        mockPatient = new Patient()
        mockBundle = new Bundle().addEntry(new Bundle.BundleEntryComponent().setResource(mockPatient))
    }

    def "addEtorTag adds the ETOR message header tag to any Bundle"() {
        given:
        def expectedSystem = "http://localcodes.org/ETOR"
        def expectedCode = "ETOR"
        def expectedDisplay = "Processed by ETOR"

        def messageHeader = new MessageHeader()
        messageHeader.setId(UUID.randomUUID().toString())
        def messageHeaderEntry = new Bundle.BundleEntryComponent().setResource(messageHeader)
        mockBundle.getEntry().add(messageHeaderEntry)

        when:
        HapiMessageConverterHelper.getInstance().addEtorTagToBundle(mockBundle) as Bundle

        then:
        def messageHeaders = mockBundle.getEntry().get(1).getResource() as MessageHeader
        def actualMessageTag = messageHeaders.getMeta().getTag()[0]

        actualMessageTag.getSystem() == expectedSystem
        actualMessageTag.getCode() == expectedCode
        actualMessageTag.getDisplay() == expectedDisplay
    }

    def "addEtorTag adds the ETOR message header tag to any Bundle when message header is missing"() {
        given:
        def expectedSystem = "http://localcodes.org/ETOR"
        def expectedCode = "ETOR"
        def expectedDisplay = "Processed by ETOR"

        when:
        HapiMessageConverterHelper.getInstance().addEtorTagToBundle(mockBundle) as Bundle

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
        def secondExpectedSystem = "http://localcodes.org/ETOR"
        def secondExpectedCode = "ETOR"
        def secondExpectedDisplay = "Processed by ETOR"

        def messageHeader = new MessageHeader()
        messageHeader.setId(UUID.randomUUID().toString())
        def meta = new Meta()
        meta.addTag(new Coding(firstExpectedSystem, firstExpectedCode, firstExpectedDisplay))
        messageHeader.setMeta(meta)
        def messageHeaderEntry = new Bundle.BundleEntryComponent().setResource(messageHeader)
        mockBundle.getEntry().add(messageHeaderEntry)

        when:
        HapiMessageConverterHelper.getInstance().addEtorTagToBundle(mockBundle) as Bundle

        then:
        def messageHeaders = mockBundle.getEntry().get(1).getResource() as MessageHeader
        def firstActualMessageTag = messageHeaders.getMeta().getTag()[0]
        def secondActualMessageTag = messageHeaders.getMeta().getTag()[1]

        firstActualMessageTag.getSystem() == firstExpectedSystem
        firstActualMessageTag.getCode() == firstExpectedCode
        firstActualMessageTag.getDisplay() == firstExpectedDisplay

        secondActualMessageTag.getSystem() == secondExpectedSystem
        secondActualMessageTag.getCode() == secondExpectedCode
        secondActualMessageTag.getDisplay() == secondExpectedDisplay
    }
}
