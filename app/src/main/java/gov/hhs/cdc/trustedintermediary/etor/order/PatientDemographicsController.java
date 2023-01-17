package gov.hhs.cdc.trustedintermediary.etor.order;

import ca.uhn.fhir.context.FhirContext;
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainRequest;
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainResponse;
import gov.hhs.cdc.trustedintermediary.wrappers.Formatter;
import gov.hhs.cdc.trustedintermediary.wrappers.FormatterProcessingException;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import java.time.ZonedDateTime;
import java.util.Map;
import javax.inject.Inject;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Enumeration;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.IntegerType;
import org.hl7.fhir.r4.model.PrimitiveType;
import org.hl7.fhir.r4.model.StringType;

/**
 * Creates an in-memory representation of an order to be ingested by the system, and return response
 * information back to the client.
 */
public class PatientDemographicsController {

    private static final PatientDemographicsController PATIENT_DEMOGRAPHICS_CONTROLLER =
            new PatientDemographicsController();

    @Inject Formatter formatter;
    @Inject Logger logger;

    static final String CONTENT_TYPE_LITERAL = "Content-Type";
    static final String APPLICATION_JSON_LITERAL = "application/json";

    private PatientDemographicsController() {}

    public static PatientDemographicsController getInstance() {
        return PATIENT_DEMOGRAPHICS_CONTROLLER;
    }

    public PatientDemographics parseDemographics(DomainRequest request) {
        logger.logInfo("Parsing patient demographics");

        FhirContext context = FhirContext.forR4();
        var pather = context.newFhirPath();
        var parser = context.newJsonParser();
        var patient = parser.parseResource(request.getBody());

        var requestIdOptional = pather.evaluateFirst(patient, "id", IdType.class);
        var patientIdOptional = pather.evaluateFirst(patient, "identifier.value", StringType.class);
        var firstNameOptional =
                pather.evaluateFirst(
                        patient, "name.where(use='official').given.first()", StringType.class);
        var lastNameOptional =
                pather.evaluateFirst(
                        patient, "name.where(use='official').family", StringType.class);
        var sexOptional = pather.evaluateFirst(patient, "gender", Enumeration.class);
        var birthDateTimeOptional =
                pather.evaluateFirst(
                        patient,
                        "birthDate.extension.where(url='http://hl7.org/fhir/StructureDefinition/patient-birthTime').value",
                        DateTimeType.class);
        var birthOrderOptional = pather.evaluateFirst(patient, "multipleBirth", IntegerType.class);

        return new PatientDemographics(
                requestIdOptional.map(IdType::getValue).orElse(null),
                patientIdOptional.map(StringType::getValue).orElse(null),
                firstNameOptional.map(StringType::getValue).orElse(null),
                lastNameOptional.map(StringType::getValue).orElse(null),
                sexOptional.map(Enumeration::getCode).orElse(null),
                birthDateTimeOptional
                        .map(birthDateTime -> ZonedDateTime.parse(birthDateTime.getValueAsString()))
                        .orElse(null),
                birthOrderOptional.map(PrimitiveType::getValue).orElse(null));
    }

    public DomainResponse constructResponse(OrderMessage orderMessage) {
        logger.logInfo("Constructing the response");
        var response = new DomainResponse(200);

        try {
            var responseBody = formatter.convertToString(orderMessage);
            response.setBody(responseBody);
        } catch (FormatterProcessingException e) {
            logger.logError("Error constructing order message", e);
            throw new RuntimeException(e);
        }

        response.setHeaders(Map.of(CONTENT_TYPE_LITERAL, APPLICATION_JSON_LITERAL));

        return response;
    }
}
