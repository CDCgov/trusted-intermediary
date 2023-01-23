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

    def "parseDemographics works"() {
        given:
        def mockRequestId = "asdf-12341-jkl-7890"
        def mockPatientId = "patientId"
        def mockFirstName = "Clarus"
        def mockLastName = "DogCow"
        def mockSex = Enumerations.AdministrativeGender.UNKNOWN.toCode()
        def mockBirthDate = "2022-12-21T08:34:27Z"
        def mockBirthNumber = 1

        def fhir = Mock(HapiFhir)

        fhir.parseResource(_ as String, _ as Class) >> new Patient()

        fhir.fhirPathEvaluateFirst(_ as IBase, "id", IdType) >> Optional.of(new IdType(mockRequestId))
        fhir.fhirPathEvaluateFirst(_ as IBase, "identifier.value", StringType) >> Optional.of(new StringType(mockPatientId))
        fhir.fhirPathEvaluateFirst(_ as IBase, "name.where(use='official').given.first()", StringType) >> Optional.of(new StringType(mockFirstName))
        fhir.fhirPathEvaluateFirst(_ as IBase, "name.where(use='official').family", StringType) >> Optional.of(new StringType(mockLastName))
        fhir.fhirPathEvaluateFirst(_ as IBase, "gender", Enumeration) >> Optional.of(new Enumeration<>(new Enumerations.AdministrativeGenderEnumFactory(), Enumerations.AdministrativeGender.fromCode(mockSex)))
        fhir.fhirPathEvaluateFirst(_ as IBase, "birthDate.extension.where(url='http://hl7.org/fhir/StructureDefinition/patient-birthTime').value", DateTimeType) >> Optional.of(new DateTimeType(mockBirthDate))
        fhir.fhirPathEvaluateFirst(_ as IBase, "multipleBirth", IntegerType) >> Optional.of(new IntegerType(mockBirthNumber))

        TestApplicationContext.register(HapiFhir, fhir)

        def request = new DomainRequest()

        TestApplicationContext.injectRegisteredImplementations()

        when:
        def patientDemographics = PatientDemographicsController.getInstance().parseDemographics(request)

        then:
        patientDemographics.getRequestId() == mockRequestId
        patientDemographics.getPatientId() == mockPatientId
        patientDemographics.getFirstName() == mockFirstName
        patientDemographics.getLastName() == mockLastName
        patientDemographics.getSex() == mockSex
        patientDemographics.getBirthDateTime() == ZonedDateTime.parse(mockBirthDate)
        patientDemographics.getBirthOrder() == mockBirthNumber
    }

    def "parseDemographics puts null into the patient demographics"() {
        given:
        def fhir = Mock(HapiFhir)

        fhir.parseResource(_ as String, _ as Class) >> new Patient()

        fhir.fhirPathEvaluateFirst(_ as IBase, "id", IdType) >> Optional.empty()
        fhir.fhirPathEvaluateFirst(_ as IBase, "identifier.value", StringType) >> Optional.empty()
        fhir.fhirPathEvaluateFirst(_ as IBase, "name.where(use='official').given.first()", StringType) >> Optional.empty()
        fhir.fhirPathEvaluateFirst(_ as IBase, "name.where(use='official').family", StringType) >> Optional.empty()
        fhir.fhirPathEvaluateFirst(_ as IBase, "gender", Enumeration) >> Optional.empty()
        fhir.fhirPathEvaluateFirst(_ as IBase, "birthDate.extension.where(url='http://hl7.org/fhir/StructureDefinition/patient-birthTime').value", DateTimeType) >> Optional.empty()
        fhir.fhirPathEvaluateFirst(_ as IBase, "multipleBirth", IntegerType) >> Optional.empty()

        TestApplicationContext.register(HapiFhir, fhir)

        def request = new DomainRequest()

        TestApplicationContext.injectRegisteredImplementations()

        when:
        def patientDemographics = PatientDemographicsController.getInstance().parseDemographics(request)

        then:
        patientDemographics.getRequestId() == null
        patientDemographics.getPatientId() == null
        patientDemographics.getFirstName() == null
        patientDemographics.getLastName() == null
        patientDemographics.getSex() == null
        patientDemographics.getBirthDateTime() == null
        patientDemographics.getBirthOrder() == null
    }

    def "the FHIR paths are correct"() {
        given:
        def mockRequestId = "asdf-12341-jkl-7890"
        def mockPatientId = "patientId"
        def mockFirstName = "Clarus"
        def mockLastName = "DogCow"
        def mockSex = Enumerations.AdministrativeGender.UNKNOWN.toCode()
        def mockBirthDate = "2022-12-21T08:34:27Z"
        def mockBirthNumber = 1

        def fhir = Mock(HapiFhir)

        def patient = new Patient()
        patient.setId(mockRequestId)
        patient.setIdentifier(List.of(new Identifier().setValue(mockPatientId), new Identifier().setValue("something else")))
        patient.setName(List.of(new HumanName().setUse(HumanName.NameUse.OFFICIAL).setFamily(mockLastName).setGiven(List.of(new StringType(mockFirstName), new StringType("Apple")))))
        patient.setGender(Enumerations.AdministrativeGender.fromCode(mockSex))
        def birthDateTime = new DateType(mockBirthDate.substring(0, mockBirthDate.indexOf("T")))
        birthDateTime.addExtension("http://hl7.org/fhir/StructureDefinition/patient-birthTime", new DateTimeType(mockBirthDate))
        patient.setBirthDateElement(birthDateTime)
        patient.setMultipleBirth(new IntegerType(mockBirthNumber))

        fhir.parseResource(_ as String, _ as Class) >> patient

        fhir.fhirPathEvaluateFirst(_ as IBase, _ as String, _ as Class) >> { IBase fhirResource, String path, Class clazz ->
            return HapiFhirImplementation.getInstance().fhirPathEvaluateFirst(fhirResource, path, clazz)
        }

        TestApplicationContext.register(HapiFhir, fhir)

        def request = new DomainRequest()

        TestApplicationContext.injectRegisteredImplementations()

        when:
        def patientDemographics = PatientDemographicsController.getInstance().parseDemographics(request)

        then:
        patientDemographics.getRequestId() == mockRequestId
        patientDemographics.getPatientId() == mockPatientId
        patientDemographics.getFirstName() == mockFirstName
        patientDemographics.getLastName() == mockLastName
        patientDemographics.getSex() == mockSex
        patientDemographics.getBirthDateTime() == ZonedDateTime.parse(mockBirthDate)
        patientDemographics.getBirthOrder() == mockBirthNumber
    }

    def "constructResponse works"() {

        given:
        def mockBody = "DogCow goes Moof"

        def formatter = Mock(Jackson)
        formatter.convertToString(_ as PatientDemographicsResponse) >> mockBody
        TestApplicationContext.register(Formatter, formatter)

        TestApplicationContext.injectRegisteredImplementations()

        when:
        def response = PatientDemographicsController.getInstance().constructResponse(new PatientDemographicsResponse("asdf-12341-jkl-7890"))

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
