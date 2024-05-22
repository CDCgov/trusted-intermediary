package gov.hhs.cdc.trustedintermediary.external.hapi

import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Coding
import org.hl7.fhir.r4.model.Extension
import org.hl7.fhir.r4.model.Identifier
import org.hl7.fhir.r4.model.MessageHeader
import org.hl7.fhir.r4.model.Meta
import org.hl7.fhir.r4.model.Patient
import org.hl7.fhir.r4.model.Provenance
import org.hl7.fhir.r4.model.ResourceType
import org.hl7.fhir.r4.model.ServiceRequest
import org.hl7.fhir.r4.model.StringType
import spock.lang.Specification

class HapiHelperTest extends Specification {

    def "resourcesInBundle return a stream of a specific type of resources in a FHIR Bundle"() {
        given:
        def patients = [
            new Patient(),
            new Patient(),
            new Patient(),
            new Patient(),
            new Patient(),
            new Patient()
        ]

        def bundle = new Bundle()
        bundle.addEntry(new Bundle.BundleEntryComponent().setResource(patients.get(0)))
        bundle.addEntry(new Bundle.BundleEntryComponent().setResource(patients.get(1)))
        bundle.addEntry(new Bundle.BundleEntryComponent().setResource(new ServiceRequest()))
        bundle.addEntry(new Bundle.BundleEntryComponent().setResource(patients.get(2)))
        bundle.addEntry(new Bundle.BundleEntryComponent().setResource(patients.get(3)))
        bundle.addEntry(new Bundle.BundleEntryComponent().setResource(new Provenance()))
        bundle.addEntry(new Bundle.BundleEntryComponent().setResource(patients.get(4)))
        bundle.addEntry(new Bundle.BundleEntryComponent().setResource(new ServiceRequest()))
        bundle.addEntry(new Bundle.BundleEntryComponent().setResource(new ServiceRequest()))
        bundle.addEntry(new Bundle.BundleEntryComponent().setResource(patients.get(5)))

        when:
        def patientStream = HapiHelper.resourcesInBundle(bundle, Patient)

        then:
        patientStream.allMatch {patients.contains(it) && it.getResourceType() == ResourceType.Patient }
    }

    // MSH - Message Header
    def "getMSHMessageHeader returns the message header from the bundle if it exists"() {
        given:
        def bundle = new Bundle()
        def messageHeader = new MessageHeader()
        def messageHeaderEntry = new Bundle.BundleEntryComponent().setResource(messageHeader)
        bundle.getEntry().add(messageHeaderEntry)

        when:
        def actualMessageHeader = HapiHelper.getMSHMessageHeader(bundle)

        then:
        actualMessageHeader == messageHeader
    }

    def "getMSHMessageHeader returns null if the message header does not exist"() {
        given:
        def bundle = new Bundle()

        when:
        def nullMessageHeader = HapiHelper.getMSHMessageHeader(bundle)

        then:
        nullMessageHeader == null
    }

    def "createMSHMessageHeader creates a new message header if it does not exist"() {
        given:
        def bundle = new Bundle()

        when:
        def actualMessageHeader = HapiHelper.createMSHMessageHeader(bundle)

        then:
        actualMessageHeader != null
        actualMessageHeader.getResourceType() == ResourceType.MessageHeader
    }

    def "addMetaTag adds message header tag to any Bundle"() {
        given:
        def expectedSystem = "expectedSystem"
        def expectedCode = "expectedCode"
        def expectedDisplay = "expectedDisplay"

        def mockBundle = new Bundle()
        HapiHelper.createMSHMessageHeader(mockBundle)

        when:
        HapiHelper.addMetaTag(mockBundle, expectedSystem, expectedCode, expectedDisplay)

        then:
        def messageHeaders = HapiHelper.resourceInBundle(mockBundle, MessageHeader)
        def actualMessageTag = messageHeaders.getMeta().getTag()[0]

        actualMessageTag.getSystem() == expectedSystem
        actualMessageTag.getCode() == expectedCode
        actualMessageTag.getDisplay() == expectedDisplay
    }

    def "addMetaTag adds message header tag to any Bundle when message header is missing"() {
        given:
        def expectedSystem = "expectedSystem"
        def expectedCode = "expectedCode"
        def expectedDisplay = "expectedDisplay"
        def mockBundle = new Bundle()

        when:
        HapiHelper.addMetaTag(mockBundle, expectedSystem, expectedCode, expectedDisplay)

        then:
        def messageHeaders = HapiHelper.resourceInBundle(mockBundle, MessageHeader)
        def actualMessageTag = messageHeaders.getMeta().getTag()[0]

        actualMessageTag.getSystem() == expectedSystem
        actualMessageTag.getCode() == expectedCode
        actualMessageTag.getDisplay() == expectedDisplay
    }

    def "addMetaTag adds the message header tag to any Bundle with existing Meta tag"() {
        given:
        def firstExpectedSystem = "firstExpectedSystem"
        def firstExpectedCode = "firstExpectedCode"
        def firstExpectedDisplay = "firstExpectedDisplay"
        def expectedSystem = "expectedSystem"
        def expectedCode = "expectedCode"
        def expectedDisplay = "expectedDisplay"

        def mockBundle = new Bundle()
        def messageHeader = new MessageHeader()
        messageHeader.setId(UUID.randomUUID().toString())
        def meta = new Meta()
        meta.addTag(new Coding(firstExpectedSystem, firstExpectedCode, firstExpectedDisplay))
        messageHeader.setMeta(meta)
        def messageHeaderEntry = new Bundle.BundleEntryComponent().setResource(messageHeader)
        mockBundle.getEntry().add(messageHeaderEntry)

        when:
        HapiHelper.addMetaTag(mockBundle, expectedSystem, expectedCode, expectedDisplay)

        then:
        def messageHeaders = HapiHelper.resourceInBundle(mockBundle, MessageHeader)
        def firstActualMessageTag = messageHeaders.getMeta().getTag()[0]
        def secondActualMessageTag = messageHeaders.getMeta().getTag()[1]

        firstActualMessageTag.getSystem() == firstExpectedSystem
        firstActualMessageTag.getCode() == firstExpectedCode
        firstActualMessageTag.getDisplay() == firstExpectedDisplay

        secondActualMessageTag.getSystem() == expectedSystem
        secondActualMessageTag.getCode() == expectedCode
        secondActualMessageTag.getDisplay() == expectedDisplay
    }

    def "addMetaTag adds the message header tag only once"() {
        given:
        def expectedSystem = "expectedSystem"
        def expectedCode = "expectedCode"
        def expectedDisplay = "expectedDisplay"
        def mockBundle = new Bundle()
        def etorTag = new Coding(expectedSystem, expectedCode, expectedDisplay)

        def messageHeader = new MessageHeader()
        messageHeader.setId(UUID.randomUUID().toString())
        def messageHeaderEntry = new Bundle.BundleEntryComponent().setResource(messageHeader)
        mockBundle.getEntry().add(messageHeaderEntry)

        when:
        HapiHelper.addMetaTag(mockBundle, expectedSystem, expectedCode, expectedDisplay)
        messageHeader.getMeta().getTag().findAll {it.system == etorTag.system}.size() == 1
        HapiHelper.addMetaTag(mockBundle, expectedSystem, expectedCode, expectedDisplay)

        then:
        messageHeader.getMeta().getTag().findAll {it.system == etorTag.system}.size() == 1
    }

    // MSH-4 - Sending Facility
    def "sending facility's get, set and create work as expected"() {
        when:
        def bundle = new Bundle()

        then:
        HapiHelper.getMSH4Organization(bundle) == null
        HapiHelper.getMSH4_1Identifier(bundle) == null

        when:
        HapiHelper.createMSHMessageHeader(bundle)

        then:
        HapiHelper.getMSH4Organization(bundle) == null

        when:
        def sendingFacility = HapiFhirHelper.createOrganization()
        HapiFhirHelper.setMSH4Organization(bundle, sendingFacility)

        then:
        HapiHelper.getMSH4Organization(bundle).equalsDeep(sendingFacility)
        HapiHelper.getMSH4_1Identifier(bundle) == null

        when:
        HapiFhirHelper.setMSH4_1Identifier(bundle, new Identifier())

        then:
        HapiHelper.getMSH4_1Identifier(bundle) != null
    }

    // MSH-5 - Receiving Application
    def "receiving application's get, set and create work as expected"() {
        given:
        def expectedReceivingApplication = HapiFhirHelper.createMessageDestinationComponent()

        when:
        def bundle = new Bundle()

        then:
        HapiHelper.getMSH5MessageDestinationComponent(bundle) == null

        when:
        HapiHelper.createMSHMessageHeader(bundle)
        def existingReceivingApplication = HapiHelper.getMSH5MessageDestinationComponent(bundle)

        then:
        !existingReceivingApplication.equalsDeep(expectedReceivingApplication)

        when:
        HapiFhirHelper.setMSH5MessageDestinationComponent(bundle, expectedReceivingApplication)
        def actualReceivingApplication = HapiHelper.getMSH5MessageDestinationComponent(bundle)

        then:
        actualReceivingApplication.equalsDeep(expectedReceivingApplication)
    }

    // MSH-9 - Message Type
    def "convert the pre-existing message type"() {
        given:
        def expectedSystem = "expectedSystem"
        def expectedCode = "expectedCode"
        def expectedDisplay = "expectedDisplay"
        def expectedCoding = new Coding(expectedSystem, expectedCode, expectedDisplay)
        def coding = new Coding("system", "code", "display")
        def mockBundle = new Bundle()
        mockBundle.addEntry(
                new Bundle.BundleEntryComponent().setResource(
                new MessageHeader().setEvent(coding)))

        when:
        HapiHelper.setMSH9Coding(mockBundle, expectedCoding)
        def convertedMessageHeader = HapiHelper.resourceInBundle(mockBundle, MessageHeader.class) as MessageHeader

        then:
        convertedMessageHeader != null
        convertedMessageHeader.getEventCoding().getSystem() == expectedSystem
        convertedMessageHeader.getEventCoding().getCode() == expectedCode
        convertedMessageHeader.getEventCoding().getDisplay() == expectedDisplay
    }

    def "adds the message type when it doesn't exist"() {
        given:
        def expectedSystem = "expectedSystem"
        def expectedCode = "expectedCode"
        def expectedDisplay = "expectedDisplay"
        def expectedCoding = new Coding(expectedSystem, expectedCode, expectedDisplay)

        when:
        def mockBundle = new Bundle()
        HapiHelper.setMSH9Coding(mockBundle, expectedCoding)

        then:
        HapiHelper.getMSH9Coding(mockBundle) == null

        when:
        HapiHelper.createMSHMessageHeader(mockBundle)
        HapiHelper.setMSH9Coding(mockBundle, expectedCoding)
        def convertedMessageHeader = HapiHelper.getMSHMessageHeader(mockBundle)

        then:
        convertedMessageHeader != null
        convertedMessageHeader.getEventCoding().getSystem() == expectedSystem
        convertedMessageHeader.getEventCoding().getCode() == expectedCode
        convertedMessageHeader.getEventCoding().getDisplay() == expectedDisplay
    }

    // MSH-9.3 - Message Type
    def "message type's get and set work as expected"() {
        given:
        def msh9_3 = "msh9_3"

        when:
        def bundle = new Bundle()

        then:
        HapiHelper.setMSH9_3Value(bundle, msh9_3)
        HapiHelper.getMSH9_3Value(bundle) == null

        when:
        HapiHelper.createMSHMessageHeader(bundle)
        HapiHelper.setMSH9_3Value(bundle, msh9_3)

        then:
        HapiHelper.getMSH9_3Value(bundle) == msh9_3
    }

    // PID - Patient Identifier
    def "patient identifier methods work as expected"() {
        given:
        def bundle = new Bundle()

        when:
        def nullPatientIdentifier = HapiHelper.getPID3Identifier(bundle)

        then:
        nullPatientIdentifier == null

        when:
        HapiFhirHelper.setPID3Identifier(bundle, new Identifier())

        then:
        noExceptionThrown()

        when:
        HapiFhirHelper.createPIDPatient(bundle)
        def newPatientIdentifier = new Identifier()
        HapiFhirHelper.setPID3Identifier(bundle, newPatientIdentifier)

        then:
        HapiHelper.getPID3Identifier(bundle) == newPatientIdentifier
    }

    // PID-3.4 - Assigning Authority
    def "patient assigning authority methods work as expected"() {
        given:
        def pid3_4 = "pid3_4"

        when:
        def bundle = new Bundle()

        then:
        HapiHelper.getPID3_4Identifier(bundle) == null

        when:
        HapiHelper.setPID3_4Value(bundle, pid3_4)

        then:
        HapiFhirHelper.getPID3_4Value(bundle) == null

        when:
        HapiFhirHelper.createPIDPatient(bundle)
        HapiFhirHelper.setPID3Identifier(bundle, new Identifier())
        HapiFhirHelper.setPID3_4Identifier(bundle, new Identifier())
        HapiHelper.setPID3_4Value(bundle, pid3_4)

        then:
        HapiFhirHelper.getPID3_4Value(bundle) == pid3_4
    }

    // PID-3.5 - Assigning Identifier Type Code
    def "patient assigning authority methods work as expected"() {
        given:
        def bundle = new Bundle()
        def pid3_5 = "pid3_5"

        when:
        HapiFhirHelper.setPID3_5Coding(bundle, new Coding())

        then:
        HapiHelper.getPID3_5Coding(bundle) == null

        when:
        HapiHelper.setPID3_5Value(bundle, pid3_5)

        then:
        HapiFhirHelper.getPID3_5Value(bundle) == null

        when:
        HapiFhirHelper.createPIDPatient(bundle)
        HapiFhirHelper.setPID3Identifier(bundle, new Identifier())
        HapiFhirHelper.setPID3_5Coding(bundle, new Coding())
        HapiHelper.setPID3_5Value(bundle, pid3_5)

        then:
        HapiFhirHelper.getPID3_5Value(bundle) == pid3_5
    }

    // PID-5 - Patient Name
    def "patient name methods work as expected"() {
        given:
        def bundle = new Bundle()

        when:
        HapiFhirHelper.setPID5Extension(bundle)

        then:
        HapiHelper.getPID5Extension(bundle) == null

        when:
        HapiFhirHelper.createPIDPatient(bundle)
        HapiFhirHelper.setPID5Extension(bundle)

        then:
        HapiHelper.getPID5Extension(bundle) != null
    }

    // PID-5.7 - Name Type Code
    def "patient name type code methods work as expected"() {
        given:
        def pid5_7 = "pid5_7"

        when:
        def bundle = new Bundle()
        HapiHelper.removePID5_7Extension(bundle)

        then:
        HapiHelper.getPID5Extension(bundle) == null

        when:
        HapiFhirHelper.createPIDPatient(bundle)
        HapiFhirHelper.setPID5Extension(bundle)
        HapiHelper.removePID5_7Extension(bundle)

        then:
        HapiFhirHelper.getPID5_7Value(bundle) == null

        when:
        HapiFhirHelper.setPID5_7Value(bundle, pid5_7)

        then:
        HapiHelper.getPID5Extension(bundle) != null
        HapiFhirHelper.getPID5_7Value(bundle) == pid5_7

        when:
        HapiHelper.removePID5_7Extension(bundle)

        then:
        HapiHelper.getPID5Extension(bundle) != null
        HapiFhirHelper.getPID5_7Value(bundle) == null
    }

    // ORC-2 - Placer Order Number
    def "orc-2.1 methods work as expected"() {
        given:
        def orc2_1 = "orc2_1"
        def orc2_2 = "orc2_2"
        def bundle = new Bundle()
        def serviceRequest = new ServiceRequest()
        bundle.addEntry(new Bundle.BundleEntryComponent().setResource(serviceRequest))

        when:
        HapiFhirHelper.setORC2Identifier(serviceRequest, new Identifier())

        then:
        HapiHelper.getORC2Identifier(serviceRequest) != null
        HapiHelper.getORC2_1Value(serviceRequest) == null
        HapiHelper.getORC2_2Value(serviceRequest) == null

        when:
        HapiHelper.setORC2_1Value(serviceRequest, orc2_1)
        HapiHelper.setORC2_2Value(serviceRequest, orc2_2)


        then:
        HapiHelper.getORC2_1Value(serviceRequest) == orc2_1
        HapiHelper.getORC2_2Value(serviceRequest) == orc2_2
    }

    // ORC-4.1 - Entity Identifier
    def "orc-4.1 methods work as expected"() {
        given:
        def orc4_1 = "orc4_1"
        def bundle = new Bundle()
        def dr = HapiFhirHelper.createDiagnosticReport(bundle)
        def sr = HapiFhirHelper.createBasedOnServiceRequest(dr)

        expect:
        HapiHelper.getORC4_1Value(sr) == null

        when:
        HapiHelper.setORC4_1Value(sr, orc4_1)

        then:
        HapiHelper.getORC4_1Value(sr) == orc4_1
    }

    // ORC-4.2 - Namespace ID
    def "orc-4.2 methods work as expected"() {
        given:
        def orc4_2 = "orc4_2"
        def bundle = new Bundle()
        def dr = HapiFhirHelper.createDiagnosticReport(bundle)
        def sr = HapiFhirHelper.createBasedOnServiceRequest(dr)

        expect:
        HapiHelper.getORC4_2Value(sr) == null

        when:
        HapiHelper.setORC4_2Value(sr, orc4_2)

        then:
        HapiHelper.getORC4_2Value(sr) == orc4_2
    }

    def "orc-21 methods work as expected"() {
        given:
        def orc21 = "orc21"
        def bundle = new Bundle()
        def dr = HapiFhirHelper.createDiagnosticReport(bundle)
        def sr = HapiFhirHelper.createBasedOnServiceRequest(dr)

        expect:
        HapiHelper.getORC21Value(sr) == null

        when:
        HapiFhirHelper.setORC21Value(sr, orc21)

        then:
        HapiHelper.getORC21Value(sr) == orc21
    }

    // HD - Hierarchic Designator
    def "getHD1Identifier returns the correct namespaceIdentifier"() {
        given:
        def expectedExtension = new Extension(HapiHelper.EXTENSION_HL7_FIELD_URL, HapiHelper.EXTENSION_HD1_DATA_TYPE)
        def expectedIdentifier = new Identifier().setExtension(List.of(expectedExtension)) as Identifier
        def otherExtension = new Extension(HapiHelper.EXTENSION_HL7_FIELD_URL, new StringType("other"))
        def otherIdentifier = new Identifier().setExtension(List.of(otherExtension)) as Identifier
        List<Identifier> identifiers = List.of(expectedIdentifier, otherIdentifier)

        expect:
        HapiHelper.getHD1Identifier(List.of(new Identifier())) == null

        when:
        def actualNamespace = HapiHelper.getHD1Identifier(identifiers)

        then:
        actualNamespace == expectedIdentifier
    }

    def "getHD1Identifier returns null if the namespace is not found"() {
        given:
        def extension = new Extension(HapiHelper.EXTENSION_HL7_FIELD_URL, new StringType("other"))
        def identifier = new Identifier().setExtension(List.of(extension)) as Identifier
        List<Identifier> identifiers = List.of(identifier)

        when:
        def actualIdentifier = HapiHelper.getHD1Identifier(identifiers)

        then:
        actualIdentifier == null
    }
}
