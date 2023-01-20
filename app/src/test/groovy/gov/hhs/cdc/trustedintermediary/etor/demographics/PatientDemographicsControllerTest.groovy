package gov.hhs.cdc.trustedintermediary.etor.demographics

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainRequest
import gov.hhs.cdc.trustedintermediary.external.jackson.Jackson
import gov.hhs.cdc.trustedintermediary.wrappers.Formatter
import gov.hhs.cdc.trustedintermediary.wrappers.FormatterProcessingException
import gov.hhs.cdc.trustedintermediary.wrappers.HapiFhir
import org.hl7.fhir.instance.model.api.IBase
import org.hl7.fhir.r4.model.DateTimeType
import org.hl7.fhir.r4.model.IdType
import org.hl7.fhir.r4.model.IntegerType
import org.hl7.fhir.r4.model.StringType
import spock.lang.Specification

class PatientDemographicsControllerTest extends Specification {

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(PatientDemographicsController, PatientDemographicsController.getInstance())
    }

    def "parseDemographics works"() {
        given:
        def mockOrderId = "asdf-12341-jkl-7890"

        def fhir = Mock(HapiFhir)
        fhir.fhirPathEvaluateFirst(_ as IBase, "id", IdType) >> Optional.of(new IdType("requestId"))
        fhir.fhirPathEvaluateFirst(_ as IBase, "identifier.value", _ as Class<? extends IBase>) >> new StringType("patientId")
        fhir.fhirPathEvaluateFirst(_ as IBase, "name.where(use='official').given.first()", _ as Class<? extends IBase>) >> new StringType("Clarus")
        fhir.fhirPathEvaluateFirst(_ as IBase, "name.where(use='official').family", _ as Class<? extends IBase>) >> new StringType("DogCow")
        fhir.fhirPathEvaluateFirst(_ as IBase, "birthDate.extension.where(url='http://hl7.org/fhir/StructureDefinition/patient-birthTime').value", _ as Class<? extends IBase>) >> new DateTimeType("2022-12-21T08:34:27Z")
        fhir.fhirPathEvaluateFirst(_ as IBase, "multipleBirth", _ as Class<? extends IBase>) >> new IntegerType(1)

        TestApplicationContext.register(HapiFhir, fhir)

        def request = new DomainRequest()

        TestApplicationContext.injectRegisteredImplementations()

        when:
        def parsedOrder = PatientDemographicsController.getInstance().parseDemographics(request)

        then:
        noExceptionThrown()
        parsedOrder.getRequestId() == mockOrderId
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
