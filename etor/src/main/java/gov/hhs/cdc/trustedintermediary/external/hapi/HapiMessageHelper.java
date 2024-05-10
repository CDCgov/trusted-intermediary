package gov.hhs.cdc.trustedintermediary.external.hapi;

import gov.hhs.cdc.trustedintermediary.plugin.path.Hl7FhirMappingPath;
import gov.hhs.cdc.trustedintermediary.wrappers.HapiFhir;
import javax.inject.Inject;
import org.hl7.fhir.r4.model.Bundle;

/**
 * Helper class for extracting various pieces of information from FHIR messages using defined FHIR
 * paths. This class utilizes the {@link HapiFhir} engine to execute FHIR path expressions against a
 * given {@link Bundle}.
 */
public class HapiMessageHelper {

    private static final HapiMessageHelper INSTANCE = new HapiMessageHelper();

    @Inject HapiFhir fhirEngine;

    public static HapiMessageHelper getInstance() {
        return INSTANCE;
    }

    private HapiMessageHelper() {}

    public String extractPlacerOrderNumber(Bundle messageBundle) {
        return fhirEngine.getStringFromFhirPath(
                messageBundle, Hl7FhirMappingPath.PLACER_ORDER_NUMBER.getFhirPath());
    }

    public String extractSendingApplicationNamespace(Bundle messageBundle) {
        return fhirEngine.getStringFromFhirPath(
                messageBundle, Hl7FhirMappingPath.SENDING_APPLICATION_NAMESPACE.getFhirPath());
    }

    public String extractSendingApplicationUniversalId(Bundle messageBundle) {
        return fhirEngine.getStringFromFhirPath(
                messageBundle, Hl7FhirMappingPath.SENDING_APPLICATION_UNIVERSAL_ID.getFhirPath());
    }

    public String extractSendingApplicationUniversalIdType(Bundle messageBundle) {
        return fhirEngine.getStringFromFhirPath(
                messageBundle, Hl7FhirMappingPath.SENDING_APPLICATION_UNIVERSAL_ID_TYPE.getFhirPath());
    }

    public String extractSendingFacilityNamespace(Bundle messageBundle) {
        return fhirEngine.getStringFromFhirPath(
                messageBundle, Hl7FhirMappingPath.SENDING_FACILITY_NAMESPACE.getFhirPath());
    }

    public String extractSendingFacilityUniversalId(Bundle messageBundle) {
        return fhirEngine.getStringFromFhirPath(
                messageBundle, Hl7FhirMappingPath.SENDING_FACILITY_UNIVERSAL_ID.getFhirPath());
    }

    public String extractSendingFacilityUniversalIdType(Bundle messageBundle) {
        return fhirEngine.getStringFromFhirPath(
                messageBundle, Hl7FhirMappingPath.SENDING_FACILITY_UNIVERSAL_ID_TYPE.getFhirPath());
    }

    public String extractReceivingApplicationNamespace(Bundle messageBundle) {
        return fhirEngine.getStringFromFhirPath(
                messageBundle, Hl7FhirMappingPath.RECEIVING_APPLICATION_NAMESPACE.getFhirPath());
    }

    public String extractReceivingApplicationUniversalId(Bundle messageBundle) {
        return fhirEngine.getStringFromFhirPath(
                messageBundle, Hl7FhirMappingPath.RECEIVING_APPLICATION_UNIVERSAL_ID.getFhirPath());
    }

    public String extractReceivingApplicationUniversalIdType(Bundle messageBundle) {
        return fhirEngine.getStringFromFhirPath(
                messageBundle, Hl7FhirMappingPath.RECEIVING_APPLICATION_UNIVERSAL_ID_TYPE.getFhirPath());
    }

    public String extractReceivingFacilityNamespace(Bundle messageBundle) {
        return fhirEngine.getStringFromFhirPath(
                messageBundle, Hl7FhirMappingPath.RECEIVING_FACILITY_NAMESPACE.getFhirPath());
    }

    public String extractReceivingFacilityUniversalId(Bundle messageBundle) {
        return fhirEngine.getStringFromFhirPath(
                messageBundle, Hl7FhirMappingPath.RECEIVING_FACILITY_UNIVERSAL_ID.getFhirPath());
    }

    public String extractReceivingFacilityUniversalIdType(Bundle messageBundle) {
        return fhirEngine.getStringFromFhirPath(
                messageBundle, Hl7FhirMappingPath.RECEIVING_FACILITY_UNIVERSAL_ID_TYPE.getFhirPath());
    }
}
