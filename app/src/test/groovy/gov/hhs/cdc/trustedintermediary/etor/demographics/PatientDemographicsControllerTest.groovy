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

        def fhir = Mock(HapiFhirImplementation)

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

    def "parseDemographics fails by the formatter"() {
        given:
        def formatter = Mock(Jackson)
        formatter.convertToObject(_ as String, _ as Class) >> { throw new FormatterProcessingException("unable to format or whatever", new Exception()) }
        TestApplicationContext.register(Formatter, formatter)

        def request = new DomainRequest()

        TestApplicationContext.injectRegisteredImplementations()

        when:
        PatientDemographicsController.getInstance().parseDemographics(request)

        then:
        thrown(RuntimeException)
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
