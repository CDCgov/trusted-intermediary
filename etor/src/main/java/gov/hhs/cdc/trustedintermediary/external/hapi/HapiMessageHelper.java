package gov.hhs.cdc.trustedintermediary.external.hapi;

import gov.hhs.cdc.trustedintermediary.plugin.path.FhirPath;
import gov.hhs.cdc.trustedintermediary.wrappers.HapiFhir;
import javax.inject.Inject;
import org.hl7.fhir.r4.model.Bundle;

public class HapiMessageHelper {

    private static final HapiMessageHelper INSTANCE = new HapiMessageHelper();

    @Inject HapiFhir fhirEngine;

    public static HapiMessageHelper getInstance() {
        return INSTANCE;
    }

    private HapiMessageHelper() {}

    public String extractPlacerOrderNumber(Bundle messageBundle) {
        return fhirEngine.getStringFromFhirPath(
                messageBundle, FhirPath.PLACER_ORDER_NUMBER.getPath());
    }

    public String extractSendingApplicationNamespace(Bundle messageBundle) {
        return fhirEngine.getStringFromFhirPath(
                messageBundle, FhirPath.SENDING_APPLICATION_NAMESPACE.getPath());
    }

    public String extractSendingApplicationUniversalId(Bundle messageBundle) {
        return fhirEngine.getStringFromFhirPath(
                messageBundle, FhirPath.SENDING_APPLICATION_UNIVERSAL_ID.getPath());
    }

    public String extractSendingApplicationUniversalIdType(Bundle messageBundle) {
        return fhirEngine.getStringFromFhirPath(
                messageBundle, FhirPath.SENDING_APPLICATION_UNIVERSAL_ID_TYPE.getPath());
    }

    public String extractSendingFacilityNamespace(Bundle messageBundle) {
        return fhirEngine.getStringFromFhirPath(
                messageBundle, FhirPath.SENDING_FACILITY_NAMESPACE.getPath());
    }

    public String extractSendingFacilityUniversalId(Bundle messageBundle) {
        return fhirEngine.getStringFromFhirPath(
                messageBundle, FhirPath.SENDING_FACILITY_UNIVERSAL_ID.getPath());
    }

    public String extractSendingFacilityUniversalIdType(Bundle messageBundle) {
        return fhirEngine.getStringFromFhirPath(
                messageBundle, FhirPath.SENDING_FACILITY_UNIVERSAL_ID_TYPE.getPath());
    }

    public String extractReceivingApplicationNamespace(Bundle messageBundle) {
        return fhirEngine.getStringFromFhirPath(
                messageBundle, FhirPath.RECEIVING_APPLICATION_NAMESPACE.getPath());
    }

    public String extractReceivingApplicationUniversalId(Bundle messageBundle) {
        return fhirEngine.getStringFromFhirPath(
                messageBundle, FhirPath.RECEIVING_APPLICATION_UNIVERSAL_ID.getPath());
    }

    public String extractReceivingApplicationUniversalIdType(Bundle messageBundle) {
        return fhirEngine.getStringFromFhirPath(
                messageBundle, FhirPath.RECEIVING_APPLICATION_UNIVERSAL_ID_TYPE.getPath());
    }

    public String extractReceivingFacilityNamespace(Bundle messageBundle) {
        return fhirEngine.getStringFromFhirPath(
                messageBundle, FhirPath.RECEIVING_FACILITY_NAMESPACE.getPath());
    }

    public String extractReceivingFacilityUniversalId(Bundle messageBundle) {
        return fhirEngine.getStringFromFhirPath(
                messageBundle, FhirPath.RECEIVING_FACILITY_UNIVERSAL_ID.getPath());
    }

    public String extractReceivingFacilityUniversalIdType(Bundle messageBundle) {
        return fhirEngine.getStringFromFhirPath(
                messageBundle, FhirPath.RECEIVING_FACILITY_UNIVERSAL_ID_TYPE.getPath());
    }
}
