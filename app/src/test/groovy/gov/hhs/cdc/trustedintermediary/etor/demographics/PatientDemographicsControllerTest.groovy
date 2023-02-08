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
        def mockNextOfKinFamilyName = "Zelda"
        def mockNextOfKinGivenName = "Link"
        def mockNextOfKinPhone = "555-555-1234"

        def fhir = Mock(HapiFhir)

        def patient = new Patient()
        patient.setId(mockFhirResourceId)
        def patientIdentifier = new Identifier().setValue("something else")
        def patientIdentifierMrn = new Identifier().setValue(mockPatientId).setType(new CodeableConcept().addCoding(new Coding().setSystem("http://terminology.hl7.org/CodeSystem/v2-0203").setCode("MR")))
        patient.setIdentifier(List.of(patientIdentifier, patientIdentifierMrn))
        patient.setName(List.of(new HumanName().setUse(HumanName.NameUse.OFFICIAL).setFamily(mockLastName).setGiven(List.of(new StringType(mockFirstName), new StringType("Apple")))))
        patient.setGender(Enumerations.AdministrativeGender.fromCode(mockSex))
        def birthDateTime = new DateType(mockBirthDate.substring(0, mockBirthDate.indexOf("T")))
        birthDateTime.addExtension("http://hl7.org/fhir/StructureDefinition/patient-birthTime", new DateTimeType(mockBirthDate))
        patient.setBirthDateElement(birthDateTime)
        patient.setMultipleBirth(new IntegerType(mockBirthNumber))
        def raceExtension = new Extension("http://hl7.org/fhir/us/core/StructureDefinition/us-core-race")
        raceExtension.addExtension(new Extension("text", new StringType("Asian")))
        patient.addExtension(raceExtension)
        def nextOfKinRelationship = List.of(new CodeableConcept().addCoding(new Coding().setSystem("http://snomed.info/sct").setCode("72705000")))
        def nextOfKinName = new HumanName().setFamily(mockNextOfKinFamilyName).addGiven(mockNextOfKinGivenName)
        def nextOfKinTelecom = List.of(new ContactPoint().setSystem(ContactPoint.ContactPointSystem.PHONE).setValue(mockNextOfKinPhone))
        patient.setContact(List.of(new Patient.ContactComponent().setRelationship(nextOfKinRelationship).setName(nextOfKinName).setTelecom(nextOfKinTelecom)))

        def bundle = new Bundle()
        bundle.addEntry(new Bundle.BundleEntryComponent().setResource(patient))

        fhir.parseResource(_ as String, _ as Class) >> bundle

        fhir.fhirPathEvaluateFirst(_ as IBase, _ as String, _ as Class) >> { IBase fhirResource, String path, Class clazz ->
            //call the actual HapiFhir implementation to ensure our FHIR paths are correct
            return HapiFhirImplementation.getInstance().fhirPathEvaluateFirst(fhirResource, path, clazz)
        }

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
        patientDemographics.getNextOfKin().firstName == mockNextOfKinGivenName
        patientDemographics.getNextOfKin().lastName == mockNextOfKinFamilyName
        patientDemographics.getNextOfKin().phoneNumber == mockNextOfKinPhone
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
        PatientDemographicsController.getInstance().constructResponse(new PatientDemographicsResponse("asdf-12341-jkl-7890", "Massachusetts", "2022-12-21T08:34:27Z", "MassGeneral", "NBS panel for Clarus the DogCow"))

        then:
        thrown(RuntimeException)
    }
}
