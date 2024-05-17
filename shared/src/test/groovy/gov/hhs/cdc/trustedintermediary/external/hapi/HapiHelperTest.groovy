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
    def "getMessageHeader returns the message header from the bundle if it exists"() {
        given:
        def bundle = new Bundle()
        def messageHeader = new MessageHeader()
        def messageHeaderEntry = new Bundle.BundleEntryComponent().setResource(messageHeader)
        bundle.getEntry().add(messageHeaderEntry)

        when:
        def actualMessageHeader = HapiHelper.getMessageHeader(bundle)

        then:
        actualMessageHeader == messageHeader
    }

    def "getMessageHeader throws a NoSuchElementException if the message header does not exist"() {
        given:
        def bundle = new Bundle()

        when:
        HapiHelper.getMessageHeader(bundle)

        then:
        thrown(NoSuchElementException)
    }

    def "createMessageHeader creates a new message header if it does not exist"() {
        given:
        def bundle = new Bundle()

        when:
        def actualMessageHeader = HapiHelper.createMessageHeader(bundle)

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
        HapiHelper.createMessageHeader(mockBundle)

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
        HapiHelper.createMessageHeader(mockBundle)

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

    // MSH-3 - Sending Application
    def "sending application's get, set and create work as expected"() {
        given:
        def bundle = new Bundle()
        def expectedSendingApplication = HapiHelper.createSendingApplication()
        HapiHelper.createMessageHeader(bundle)

        expect:
        def existingSendingApplication = HapiHelper.getSendingApplication(bundle)
        !existingSendingApplication.equalsDeep(expectedSendingApplication)

        when:
        HapiHelper.setSendingApplication(bundle, expectedSendingApplication)
        def actualSendingApplication = HapiHelper.getSendingApplication(bundle)

        then:
        actualSendingApplication.equalsDeep(expectedSendingApplication)
    }

    // MSH-4 - Sending Facility
    def "sending facility's get, set and create work as expected"() {
        given:
        def bundle = new Bundle()
        HapiHelper.createMessageHeader(bundle)

        expect:
        HapiHelper.getSendingFacility(bundle) == null

        when:
        def sendingFacility = HapiHelper.createOrganization()
        HapiHelper.setSendingFacility(bundle, sendingFacility)

        then:
        HapiHelper.getSendingFacility(bundle).equalsDeep(sendingFacility)
    }

    // MSH-5 - Receiving Application
    def "receiving application's get, set and create work as expected"() {
        given:
        def bundle = new Bundle()
        def expectedReceivingApplication = HapiHelper.createMessageDestinationComponent()
        HapiHelper.createMessageHeader(bundle)

        expect:
        def existingReceivingApplication = HapiHelper.getReceivingApplication(bundle)
        !existingReceivingApplication.equalsDeep(expectedReceivingApplication)

        when:
        HapiHelper.setReceivingApplication(bundle, expectedReceivingApplication)
        def actualReceivingApplication = HapiHelper.getReceivingApplication(bundle)

        then:
        actualReceivingApplication.equalsDeep(expectedReceivingApplication)
    }

    // MSH-6 - Receiving Facility
    def "receiving facility's get, set and create work as expected"() {
        given:
        def bundle = new Bundle()
        HapiHelper.createMessageHeader(bundle)

        expect:
        HapiHelper.getReceivingFacility(bundle) == null

        when:
        def receivingFacility = HapiHelper.createOrganization()
        HapiHelper.setReceivingFacility(bundle, receivingFacility)

        then:
        HapiHelper.getReceivingFacility(bundle).equalsDeep(receivingFacility)
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
        HapiHelper.setMessageTypeCoding(mockBundle, expectedCoding)
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
        def mockBundle = new Bundle()
        HapiHelper.createMessageHeader(mockBundle)

        when:
        HapiHelper.setMessageTypeCoding(mockBundle, expectedCoding)
        def convertedMessageHeader = HapiHelper.getMessageHeader(mockBundle)

        then:
        convertedMessageHeader != null
        convertedMessageHeader.getEventCoding().getSystem() == expectedSystem
        convertedMessageHeader.getEventCoding().getCode() == expectedCode
        convertedMessageHeader.getEventCoding().getDisplay() == expectedDisplay
    }

    // MSH-9.3 - Message Type
    def "message type's get and set work as expected"() {
        given:
        def bundle = new Bundle()
        def msh9_3 = "msh9_3"
        HapiHelper.createMessageHeader(bundle)

        when:
        HapiHelper.setMSH9_3Value(bundle, msh9_3)

        then:
        HapiHelper.getMSH9_3Value(bundle) == msh9_3
    }

    // PID - Patient Identifier
    def "patient identifier methods work as expected"() {
        given:
        def bundle = new Bundle()

        when:
        def nullPatientIdentifier = HapiHelper.getPatientIdentifier(bundle)

        then:
        nullPatientIdentifier == null

        when:
        HapiHelper.setPatientIdentifier(bundle, new Identifier())

        then:
        noExceptionThrown()

        when:
        HapiHelper.createPatient(bundle)
        def newPatientIdentifier = new Identifier()
        HapiHelper.setPatientIdentifier(bundle, newPatientIdentifier)

        then:
        HapiHelper.getPatientIdentifier(bundle) == newPatientIdentifier
    }

    // PID-3.4 - Assigning Authority
    def "patient assigning authority methods work as expected"() {
        given:
        def bundle = new Bundle()
        def pid3_4 = "pid3_4"

        when:
        HapiHelper.setPID3_4Identifier(bundle, new Identifier())

        then:
        HapiHelper.getPID3_4Identifier(bundle) == null

        when:
        HapiHelper.setPID3_4Value(bundle, pid3_4)

        then:
        HapiHelper.getPID3_4Value(bundle) == null

        when:
        HapiHelper.createPatient(bundle)
        HapiHelper.setPatientIdentifier(bundle, new Identifier())
        HapiHelper.setPID3_4Identifier(bundle, new Identifier())

        then:
        HapiHelper.getPID3_4Value(bundle) == null

        when:
        HapiHelper.setPID3_4Value(bundle, pid3_4)

        then:
        HapiHelper.getPID3_4Value(bundle) == pid3_4
    }

    // PID-3.5 - Assigning Identifier Type Code
    def "patient assigning authority methods work as expected"() {
        given:
        def bundle = new Bundle()
        def pid3_5 = "pid3_5"

        when:
        HapiHelper.setPID3_5Coding(bundle, new Coding())

        then:
        HapiHelper.getPID3_5Coding(bundle) == null

        when:
        HapiHelper.setPID3_5Value(bundle, pid3_5)

        then:
        HapiHelper.getPID3_5Value(bundle) == null

        when:
        HapiHelper.createPatient(bundle)
        HapiHelper.setPatientIdentifier(bundle, new Identifier())
        HapiHelper.setPID3_5Coding(bundle, new Coding())

        then:
        HapiHelper.getPID3_5Value(bundle) == null

        when:
        HapiHelper.setPID3_5Value(bundle, pid3_5)

        then:
        HapiHelper.getPID3_5Value(bundle) == pid3_5
    }

    // PID-5 - Patient Name
    def "patient name methods work as expected"() {
        given:
        def bundle = new Bundle()

        when:
        HapiHelper.setPID5Extension(bundle)

        then:
        HapiHelper.getPID5Extension(bundle) == null

        when:
        HapiHelper.createPatient(bundle)
        HapiHelper.setPID5Extension(bundle)

        then:
        HapiHelper.getPID5Extension(bundle) != null
    }

    // PID-5.7 - Name Type Code
    def "patient name type code methods work as expected"() {
        given:
        def bundle = new Bundle()
        def pid5_7 = "pid5_7"

        when:
        HapiHelper.removePID5_7Extension(bundle)
        HapiHelper.setPID5_7Value(bundle, pid5_7)

        then:
        HapiHelper.getPID5_7Value(bundle) == null

        when:
        HapiHelper.createPatient(bundle)
        HapiHelper.setPID5Extension(bundle)
        HapiHelper.removePID5_7Extension(bundle)

        then:
        HapiHelper.getPID5_7Value(bundle) == null

        when:
        HapiHelper.setPID5_7Value(bundle, pid5_7)

        then:
        HapiHelper.getPID5_7Value(bundle) == pid5_7

        when:
        HapiHelper.setPID5_7Value(bundle, "")

        then:
        HapiHelper.getPID5_7Value(bundle) == ""

        when:
        HapiHelper.removePID5_7Extension(bundle)

        then:
        HapiHelper.getPID5_7Value(bundle) == null
    }

    // ORC - Common Order
    def "DiagnosticReport methods work as expected"() {
        given:
        def bundle = new Bundle()

        expect:
        HapiHelper.getDiagnosticReport(bundle) == null

        when:
        HapiHelper.createDiagnosticReport(bundle)

        then:
        HapiHelper.getDiagnosticReport(bundle) != null
    }

    def "BasedOnServiceRequest methods work as expected"() {
        given:
        def bundle = new Bundle()
        def dr = HapiHelper.createDiagnosticReport(bundle)

        expect:
        HapiHelper.getServiceRequestBasedOn(dr) == null
        dr.getBasedOnFirstRep().getResource() == null

        when:
        def sr = HapiHelper.createBasedOnServiceRequest(dr)

        then:
        sr != null
        dr.getBasedOn().size() == 1
        dr.getBasedOnFirstRep().getResource() == sr
    }

    // ORC-4.1 - Entity Identifier
    def "orc-4.1 methods work as expected"() {
        given:
        def orc4_1 = "orc4_1"
        def bundle = new Bundle()
        def dr = HapiHelper.createDiagnosticReport(bundle)
        def sr = HapiHelper.createBasedOnServiceRequest(dr)

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
        def dr = HapiHelper.createDiagnosticReport(bundle)
        def sr = HapiHelper.createBasedOnServiceRequest(dr)

        expect:
        HapiHelper.getORC4_2Value(sr) == null

        when:
        HapiHelper.setORC4_2Value(sr, orc4_2)

        then:
        HapiHelper.getORC4_2Value(sr) == orc4_2
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
