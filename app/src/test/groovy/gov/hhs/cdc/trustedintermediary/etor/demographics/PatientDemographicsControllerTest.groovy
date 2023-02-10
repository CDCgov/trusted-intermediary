package gov.hhs.cdc.trustedintermediary.etor.demographics

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainRequest
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiFhirImplementation
import gov.hhs.cdc.trustedintermediary.external.jackson.Jackson
import gov.hhs.cdc.trustedintermediary.wrappers.Formatter
import gov.hhs.cdc.trustedintermediary.wrappers.FormatterProcessingException
import gov.hhs.cdc.trustedintermediary.wrappers.HapiFhir
import org.hl7.fhir.instance.model.api.IBase
import org.hl7.fhir.r4.model.*
import spock.lang.Specification

import java.time.ZonedDateTime

class PatientDemographicsControllerTest extends Specification {

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(PatientDemographicsController, PatientDemographicsController.getInstance())
    }

    def "parseDemographics extracts the optional and correctly interprets the value"() {
        given:
        def mockRequestId = "asdf-12341-jkl-7890"
        def mockPatientId = "patientId"
        def mockFirstName = "Clarus"
        def mockLastName = "DogCow"
        def mockSex = Enumerations.AdministrativeGender.UNKNOWN.toCode()
        def mockBirthDate = "2022-12-21T08:34:27Z"
        def mockBirthNumber = 1
        def mockRace = "Asian"
        def mockNextOfKin = new NextOfKin("Jaina", "Solo", "555-555-5555")

        def fhir = Mock(HapiFhir)

        fhir.parseResource(_ as String, _ as Class) >> new Bundle()

        fhir.fhirPathEvaluateFirst(_ as IBase, PatientDemographicsController.RESOURCE_ID_FHIR_PATH, IdType) >> Optional.of(new IdType(mockRequestId))
        fhir.fhirPathEvaluateFirst(_ as IBase, PatientDemographicsController.PATIENT_ID_FHIR_PATH, StringType) >> Optional.of(new StringType(mockPatientId))
        fhir.fhirPathEvaluateFirst(_ as IBase, PatientDemographicsController.PATIENT_FIRST_NAME_FHIR_PATH, StringType) >> Optional.of(new StringType(mockFirstName))
        fhir.fhirPathEvaluateFirst(_ as IBase, PatientDemographicsController.PATIENT_LAST_NAME_FHIR_PATH, StringType) >> Optional.of(new StringType(mockLastName))
        fhir.fhirPathEvaluateFirst(_ as IBase, PatientDemographicsController.PATIENT_SEX_FHIR_PATH, Enumeration) >> Optional.of(new Enumeration<>(new Enumerations.AdministrativeGenderEnumFactory(), Enumerations.AdministrativeGender.fromCode(mockSex)))
        fhir.fhirPathEvaluateFirst(_ as IBase, PatientDemographicsController.PATIENT_BIRTH_DATE_TIME_FHIR_PATH, DateTimeType) >> Optional.of(new DateTimeType(mockBirthDate))
        fhir.fhirPathEvaluateFirst(_ as IBase, PatientDemographicsController.PATIENT_BIRTH_ORDER_FHIR_PATH, IntegerType) >> Optional.of(new IntegerType(mockBirthNumber))
        fhir.fhirPathEvaluateFirst(_ as IBase, PatientDemographicsController.PATIENT_RACE_FHIR_PATH, StringType) >> Optional.of(new StringType(mockRace))
        fhir.fhirPathEvaluateFirst(_ as IBase, PatientDemographicsController.PATIENT_NEXT_OF_KIN_FIRST_NAME_FHIR_PATH, StringType) >> Optional.of(new StringType(mockNextOfKin.firstName))
        fhir.fhirPathEvaluateFirst(_ as IBase, PatientDemographicsController.PATIENT_NEXT_OF_KIN_LAST_NAME_FHIR_PATH, StringType) >> Optional.of(new StringType(mockNextOfKin.lastName))
        fhir.fhirPathEvaluateFirst(_ as IBase, PatientDemographicsController.PATIENT_NEXT_OF_KIN_PHONE_NUMBER_FHIR_PATH, StringType) >> Optional.of(new StringType(mockNextOfKin.phoneNumber))

        TestApplicationContext.register(HapiFhir, fhir)

        def request = new DomainRequest()

        TestApplicationContext.injectRegisteredImplementations()

        when:
        def patientDemographics = PatientDemographicsController.getInstance().parseDemographics(request)

        then:
        patientDemographics.getFhirResourceId() == mockRequestId
        patientDemographics.getPatientId() == mockPatientId
        patientDemographics.getFirstName() == mockFirstName
        patientDemographics.getLastName() == mockLastName
        patientDemographics.getSex() == mockSex
        patientDemographics.getBirthDateTime() == ZonedDateTime.parse(mockBirthDate)
        patientDemographics.getBirthOrder() == mockBirthNumber
        patientDemographics.getRace() == mockRace
        patientDemographics.getNextOfKin().firstName == mockNextOfKin.firstName
        patientDemographics.getNextOfKin().lastName == mockNextOfKin.lastName
        patientDemographics.getNextOfKin().phoneNumber == mockNextOfKin.phoneNumber
    }

    def "parseDemographics puts null into the patient demographics"() {
        given:
        def fhir = Mock(HapiFhir)

        fhir.parseResource(_ as String, _ as Class) >> new Bundle()

        fhir.fhirPathEvaluateFirst(_ as IBase, PatientDemographicsController.RESOURCE_ID_FHIR_PATH, IdType) >> Optional.empty()
        fhir.fhirPathEvaluateFirst(_ as IBase, PatientDemographicsController.PATIENT_ID_FHIR_PATH, StringType) >> Optional.empty()
        fhir.fhirPathEvaluateFirst(_ as IBase, PatientDemographicsController.PATIENT_FIRST_NAME_FHIR_PATH, StringType) >> Optional.empty()
        fhir.fhirPathEvaluateFirst(_ as IBase, PatientDemographicsController.PATIENT_LAST_NAME_FHIR_PATH, StringType) >> Optional.empty()
        fhir.fhirPathEvaluateFirst(_ as IBase, PatientDemographicsController.PATIENT_SEX_FHIR_PATH, Enumeration) >> Optional.empty()
        fhir.fhirPathEvaluateFirst(_ as IBase, PatientDemographicsController.PATIENT_BIRTH_DATE_TIME_FHIR_PATH, DateTimeType) >> Optional.empty()
        fhir.fhirPathEvaluateFirst(_ as IBase, PatientDemographicsController.PATIENT_BIRTH_ORDER_FHIR_PATH, IntegerType) >> Optional.empty()
        fhir.fhirPathEvaluateFirst(_ as IBase, PatientDemographicsController.PATIENT_RACE_FHIR_PATH, StringType) >> Optional.empty()
        fhir.fhirPathEvaluateFirst(_ as IBase, PatientDemographicsController.PATIENT_NEXT_OF_KIN_FIRST_NAME_FHIR_PATH, StringType) >> Optional.empty()
        fhir.fhirPathEvaluateFirst(_ as IBase, PatientDemographicsController.PATIENT_NEXT_OF_KIN_LAST_NAME_FHIR_PATH, StringType) >> Optional.empty()
        fhir.fhirPathEvaluateFirst(_ as IBase, PatientDemographicsController.PATIENT_NEXT_OF_KIN_PHONE_NUMBER_FHIR_PATH, StringType) >> Optional.empty()

        TestApplicationContext.register(HapiFhir, fhir)

        def request = new DomainRequest()

        TestApplicationContext.injectRegisteredImplementations()

        when:
        def patientDemographics = PatientDemographicsController.getInstance().parseDemographics(request)

        then:
        patientDemographics.getFhirResourceId() == null
        patientDemographics.getPatientId() == null
        patientDemographics.getFirstName() == null
        patientDemographics.getLastName() == null
        patientDemographics.getSex() == null
        patientDemographics.getBirthDateTime() == null
        patientDemographics.getBirthOrder() == null
        patientDemographics.getRace() == null
        patientDemographics.getNextOfKin().firstName == null
        patientDemographics.getNextOfKin().lastName == null
        patientDemographics.getNextOfKin().phoneNumber == null
    }

    def "the FHIR paths are correct"() {
        given:
        def mockFhirResourceId = "asdf-12341-jkl-7890"
        def mockPatientId = "patientId"
        def mockFirstName = "Clarus"
        def mockLastName = "DogCow"
        def mockSex = Enumerations.AdministrativeGender.UNKNOWN.toCode()
        def mockBirthDate = "2022-12-21T08:34:27Z"
        def mockBirthNumber = 1
        def mockRace = "Asian"
        def mockNextOfKinFirstName = "Link"
        def mockNextOfKinLastName = "Zelda"
        def mockNextOfKinPhone = "555-555-1234"

        def fhir = Mock(HapiFhir)

        def bundle = constructTestFhirBundle(mockFhirResourceId, mockPatientId, mockFirstName, mockLastName, mockSex, mockBirthDate, mockBirthNumber, mockRace, mockNextOfKinFirstName, mockNextOfKinLastName, mockNextOfKinPhone)

        fhir.parseResource(_ as String, _ as Class) >> bundle

        mockFhirToCallRealFhirPathImplementation(fhir)

        TestApplicationContext.register(HapiFhir, fhir)

        def request = new DomainRequest()

        TestApplicationContext.injectRegisteredImplementations()

        when:
        def patientDemographics = PatientDemographicsController.getInstance().parseDemographics(request)

        then:
        patientDemographics.getFhirResourceId() == mockFhirResourceId
        patientDemographics.getPatientId() == mockPatientId
        patientDemographics.getFirstName() == mockFirstName
        patientDemographics.getLastName() == mockLastName
        patientDemographics.getSex() == mockSex
        patientDemographics.getBirthDateTime() == ZonedDateTime.parse(mockBirthDate)
        patientDemographics.getBirthOrder() == mockBirthNumber
        patientDemographics.getRace() == mockRace
        patientDemographics.getNextOfKin().firstName == mockNextOfKinFirstName
        patientDemographics.getNextOfKin().lastName == mockNextOfKinLastName
        patientDemographics.getNextOfKin().phoneNumber == mockNextOfKinPhone
    }

    def "the FHIR paths extract next of kin contact before mother for next of kin"() {
        given:
        def mockNextOfKinFirstName = "Link"
        def mockNextOfKinLastName = "Zelda"
        def mockNextOfKinPhone = "555-867-5309"

        def bundle = constructTestFhirBundle("asdf-12341-jkl-7890", "patientId", "Clarus", "DogCow", Enumerations.AdministrativeGender.UNKNOWN.toCode(), "2022-12-21T08:34:27Z", 1, "Asian", "Darth", "Vader", "555-sta-wars")
        Patient patient = bundle.getEntry().get(0).getResource() as Patient
        def mother = constructTestFhirPatientContact("Mother", "Brain", "555-123-4567", "http://terminology.hl7.org/CodeSystem/v3-RoleCode", "MTH")
        def nextOfKin = constructTestFhirPatientContact(mockNextOfKinFirstName, mockNextOfKinLastName, mockNextOfKinPhone, "http://terminology.hl7.org/CodeSystem/v2-0131", "N")
        patient.getContact().add(mother)
        patient.getContact().add(nextOfKin)

        def fhir = Mock(HapiFhir)

        fhir.parseResource(_ as String, _ as Class) >> bundle

        mockFhirToCallRealFhirPathImplementation(fhir)

        TestApplicationContext.register(HapiFhir, fhir)

        TestApplicationContext.injectRegisteredImplementations()

        def request = new DomainRequest()

        when:
        def patientDemographics = PatientDemographicsController.getInstance().parseDemographics(request)

        then:
        patientDemographics.getNextOfKin().firstName == mockNextOfKinFirstName
        patientDemographics.getNextOfKin().lastName == mockNextOfKinLastName
        patientDemographics.getNextOfKin().phoneNumber == mockNextOfKinPhone
    }

    def "the FHIR paths extract mother contact before father for next of kin"() {
        given:
        def mockMotherFirstName = "Link"
        def mockMotherLastName = "Zelda"
        def mockMotherPhone = "555-867-5309"

        def bundle = constructTestFhirBundle("asdf-12341-jkl-7890", "patientId", "Clarus", "DogCow", Enumerations.AdministrativeGender.UNKNOWN.toCode(), "2022-12-21T08:34:27Z", 1, "Asian", "Darth", "Vader", "555-sta-wars")
        Patient patient = bundle.getEntry().get(0).getResource() as Patient
        def father = constructTestFhirPatientContact("Darth", "Vader", "555-123-4567", "http://terminology.hl7.org/CodeSystem/v3-RoleCode", "FTH")
        def mother = constructTestFhirPatientContact(mockMotherFirstName, mockMotherLastName, mockMotherPhone, "http://terminology.hl7.org/CodeSystem/v3-RoleCode", "MTH")
        patient.getContact().add(father)
        patient.getContact().add(mother)

        def fhir = Mock(HapiFhir)

        fhir.parseResource(_ as String, _ as Class) >> bundle

        mockFhirToCallRealFhirPathImplementation(fhir)

        TestApplicationContext.register(HapiFhir, fhir)

        TestApplicationContext.injectRegisteredImplementations()

        def request = new DomainRequest()

        when:
        def patientDemographics = PatientDemographicsController.getInstance().parseDemographics(request)

        then:
        patientDemographics.getNextOfKin().firstName == mockMotherFirstName
        patientDemographics.getNextOfKin().lastName == mockMotherLastName
        patientDemographics.getNextOfKin().phoneNumber == mockMotherPhone
    }

    def "constructResponse works"() {

        given:
        def mockBody = "DogCow goes Moof"

        def formatter = Mock(Jackson)
        formatter.convertToString(_ as PatientDemographicsResponse) >> mockBody
        TestApplicationContext.register(Formatter, formatter)

        TestApplicationContext.injectRegisteredImplementations()

        when:
        def response = PatientDemographicsController.getInstance().constructResponse(new PatientDemographicsResponse("asdf-12341-jkl-7890", "blkjh-7685"))

        then:
        response.getBody() == mockBody
        response.getStatusCode() == 200
        response.getHeaders().get(PatientDemographicsController.CONTENT_TYPE_LITERAL) == PatientDemographicsController.APPLICATION_JSON_LITERAL
    }

    def "constructResponse fails to make the JSON"() {

        given:
        def formatter = Mock(Jackson)
        formatter.convertToString(_ as PatientDemographicsResponse) >> { throw new FormatterProcessingException("couldn't make the JSON", new Exception()) }
        TestApplicationContext.register(Formatter, formatter)

        TestApplicationContext.injectRegisteredImplementations()

        when:
        PatientDemographicsController.getInstance().constructResponse(new PatientDemographicsResponse("asdf-12341-jkl-7890", "asdf1234"))

        then:
        thrown(RuntimeException)
    }

    Bundle constructTestFhirBundle(String mockFhirResourceId, String mockPatientId, String mockFirstName, String mockLastName, String mockSex, String mockBirthDate, int mockBirthNumber, String mockRace, String mockNextOfKinFirstName, String mockNextOfKinLastName, String mockNextOfKinPhone) {
        def patient = new Patient()
        patient.setId(mockFhirResourceId)
        def patientIdentifier = new Identifier().setValue("something else")
        def patientIdentifierMrn = new Identifier().setValue(mockPatientId).setType(new CodeableConcept().addCoding(new Coding().setSystem("http://terminology.hl7.org/CodeSystem/v2-0203").setCode("MR")))
        patient.setIdentifier([
            patientIdentifier,
            patientIdentifierMrn
        ])
        patient.setName([
            new HumanName().setUse(HumanName.NameUse.OFFICIAL).setFamily(mockLastName).setGiven([
                new StringType(mockFirstName),
                new StringType("Apple")
            ])
        ])
        patient.setGender(Enumerations.AdministrativeGender.fromCode(mockSex))
        def birthDateTime = new DateType(mockBirthDate.substring(0, mockBirthDate.indexOf("T")))
        birthDateTime.addExtension("http://hl7.org/fhir/StructureDefinition/patient-birthTime", new DateTimeType(mockBirthDate))
        patient.setBirthDateElement(birthDateTime)
        patient.setMultipleBirth(new IntegerType(mockBirthNumber))
        def raceExtension = new Extension("http://hl7.org/fhir/us/core/StructureDefinition/us-core-race")
        raceExtension.addExtension(new Extension("text", new StringType(mockRace)))
        patient.addExtension(raceExtension)
        def nextOfKin = constructTestFhirPatientContact(mockNextOfKinFirstName, mockNextOfKinLastName, mockNextOfKinPhone, "http://snomed.info/sct", "66839005")  //is father
        patient.setContact([nextOfKin])

        def bundle = new Bundle()
        bundle.addEntry(new Bundle.BundleEntryComponent().setResource(patient))

        return bundle
    }

    Patient.ContactComponent constructTestFhirPatientContact(String firstName, String lastName, String phoneNumber, String relationshipSystem, String relationshipCode) {
        def nextOfKinRelationship = [
            new CodeableConcept().addCoding(new Coding().setSystem(relationshipSystem).setCode(relationshipCode))
        ]
        def nextOfKinName = new HumanName().setFamily(lastName).addGiven(firstName)
        def nextOfKinTelecom = [
            new ContactPoint().setSystem(ContactPoint.ContactPointSystem.PHONE).setValue(phoneNumber)
        ]

        return new Patient.ContactComponent().setRelationship(nextOfKinRelationship).setName(nextOfKinName).setTelecom(nextOfKinTelecom)
    }

    def mockFhirToCallRealFhirPathImplementation(HapiFhir fhir) {
        fhir.fhirPathEvaluateFirst(_ as IBase, _ as String, _ as Class) >> { IBase fhirResource, String path, Class clazz ->
            //call the actual HapiFhir implementation to ensure our FHIR paths are correct
            return HapiFhirImplementation.getInstance().fhirPathEvaluateFirst(fhirResource, path, clazz)
        }
    }
}
