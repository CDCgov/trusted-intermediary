package gov.hhs.cdc.trustedintermediary.etor.demographics;

import gov.hhs.cdc.trustedintermediary.domainconnector.DomainRequest;
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainResponse;
import gov.hhs.cdc.trustedintermediary.wrappers.Formatter;
import gov.hhs.cdc.trustedintermediary.wrappers.FormatterProcessingException;
import gov.hhs.cdc.trustedintermediary.wrappers.HapiFhir;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import java.time.ZonedDateTime;
import java.util.Map;
import javax.inject.Inject;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Enumeration;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.IntegerType;
import org.hl7.fhir.r4.model.StringType;

/**
 * Creates an in-memory representation of patient demographics to be ingested by the system, and
 * return response information back to the client.
 */
public class PatientDemographicsController {

    private static final PatientDemographicsController PATIENT_DEMOGRAPHICS_CONTROLLER =
            new PatientDemographicsController();
    static final String PATIENT_IN_BUNDLE_FHIR_PATH = "entry.resource.ofType(Patient).";
    static final String CONTENT_TYPE_LITERAL = "Content-Type";
    static final String APPLICATION_JSON_LITERAL = "application/json";
    private static final String IS_NEXT_OF_KIN =
            "(system='http://terminology.hl7.org/CodeSystem/v2-0131' and code='N')";
    private static final String IS_MOTHER =
            "(system='http://terminology.hl7.org/CodeSystem/v3-RoleCode' and code='MTH' or system='http://snomed.info/sct' and code='72705000')";
    private static final String IS_FATHER =
            "(system='http://terminology.hl7.org/CodeSystem/v3-RoleCode' and code='FTH' or system='http://snomed.info/sct' and code='66839005')";
    static final String NEXT_OF_KIN_FHIR_PATH =
            "contact.where(relationship.where(coding.where("
                    + IS_NEXT_OF_KIN
                    + " or "
                    + IS_MOTHER
                    + " or "
                    + IS_FATHER
                    + ").exists()).exists()).";

    @Inject HapiFhir fhir;
    @Inject Formatter formatter;
    @Inject Logger logger;

    private PatientDemographicsController() {}

    public static PatientDemographicsController getInstance() {
        return PATIENT_DEMOGRAPHICS_CONTROLLER;
    }

    public PatientDemographics parseDemographics(DomainRequest request) {
        logger.logInfo("Parsing patient demographics");

        var fhirBundle = fhir.parseResource(request.getBody(), Bundle.class);

        var fhirResourceIdOptional =
                fhir.fhirPathEvaluateFirst(
                        fhirBundle, PATIENT_IN_BUNDLE_FHIR_PATH + "id", IdType.class);
        var patientIdOptional =
                fhir.fhirPathEvaluateFirst(
                        fhirBundle,
                        PATIENT_IN_BUNDLE_FHIR_PATH + "identifier.value",
                        StringType.class);
        var firstNameOptional =
                fhir.fhirPathEvaluateFirst(
                        fhirBundle,
                        PATIENT_IN_BUNDLE_FHIR_PATH + "name.where(use='official').given.first()",
                        StringType.class);
        var lastNameOptional =
                fhir.fhirPathEvaluateFirst(
                        fhirBundle,
                        PATIENT_IN_BUNDLE_FHIR_PATH + "name.where(use='official').family",
                        StringType.class);
        var sexOptional =
                fhir.fhirPathEvaluateFirst(
                        fhirBundle, PATIENT_IN_BUNDLE_FHIR_PATH + "gender", Enumeration.class);
        var birthDateTimeOptional =
                fhir.fhirPathEvaluateFirst(
                        fhirBundle,
                        PATIENT_IN_BUNDLE_FHIR_PATH
                                + "birthDate.extension.where(url='http://hl7.org/fhir/StructureDefinition/patient-birthTime').value",
                        DateTimeType.class);
        var birthOrderOptional =
                fhir.fhirPathEvaluateFirst(
                        fhirBundle,
                        PATIENT_IN_BUNDLE_FHIR_PATH + "multipleBirth",
                        IntegerType.class);
        var raceOptional =
                fhir.fhirPathEvaluateFirst(
                        fhirBundle,
                        PATIENT_IN_BUNDLE_FHIR_PATH
                                + "extension.where(url='http://hl7.org/fhir/us/core/StructureDefinition/us-core-race').extension.where(url='text').value",
                        StringType.class);

        var nextOfKin = parseOutNextOfKin(fhirBundle);
        // logging to check value
        logger.logInfo("Next of Kin = " + nextOfKin);

        return new PatientDemographics(
                fhirResourceIdOptional.map(IdType::getValue).orElse(null),
                patientIdOptional.map(StringType::getValue).orElse(null),
                firstNameOptional.map(StringType::getValue).orElse(null),
                lastNameOptional.map(StringType::getValue).orElse(null),
                sexOptional.map(Enumeration::getCode).orElse(null),
                birthDateTimeOptional
                        .map(birthDateTime -> ZonedDateTime.parse(birthDateTime.getValueAsString()))
                        .orElse(null),
                birthOrderOptional.map(IntegerType::getValue).orElse(null),
                raceOptional.map(StringType::getValue).orElse(null),
                nextOfKin);
    }

    public DomainResponse constructResponse(
            PatientDemographicsResponse patientDemographicsResponse) {
        logger.logInfo("Constructing the response");
        var response = new DomainResponse(200);

        try {
            var responseBody = formatter.convertToString(patientDemographicsResponse);
            response.setBody(responseBody);
        } catch (FormatterProcessingException e) {
            logger.logError("Error constructing demographics response", e);
            throw new RuntimeException(e);
        }

        response.setHeaders(Map.of(CONTENT_TYPE_LITERAL, APPLICATION_JSON_LITERAL));

        return response;
    }

    private NextOfKin parseOutNextOfKin(final Bundle fhirBundle) {
        var firstNameOptional =
                fhir.fhirPathEvaluateFirst(
                        fhirBundle,
                        PATIENT_IN_BUNDLE_FHIR_PATH + NEXT_OF_KIN_FHIR_PATH + "name.given.first()",
                        StringType.class);
        var lastNameOptional =
                fhir.fhirPathEvaluateFirst(
                        fhirBundle,
                        PATIENT_IN_BUNDLE_FHIR_PATH + NEXT_OF_KIN_FHIR_PATH + "name.family",
                        StringType.class);
        var phoneNumberOptional =
                fhir.fhirPathEvaluateFirst(
                        fhirBundle,
                        PATIENT_IN_BUNDLE_FHIR_PATH
                                + NEXT_OF_KIN_FHIR_PATH
                                + "telecom.where(system='phone').value",
                        StringType.class);

        return new NextOfKin(
                firstNameOptional.map(StringType::getValue).orElse(null),
                lastNameOptional.map(StringType::getValue).orElse(null),
                phoneNumberOptional.map(StringType::getValue).orElse(null));
    }
}
