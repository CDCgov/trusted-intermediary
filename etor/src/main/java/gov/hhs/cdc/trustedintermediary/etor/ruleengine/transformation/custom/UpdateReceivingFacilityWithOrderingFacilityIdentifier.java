package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom;

import gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.CustomFhirTransformation;
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiHelper;
import gov.hhs.cdc.trustedintermediary.wrappers.HealthData;
import java.util.Map;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.hl7.fhir.r4.model.ServiceRequest;

/**
 * Updates the receiving facility (MSH-6) to value in Ordering Facility Name's Organization
 * Identifier (ORC-21.10).
 */
public class UpdateReceivingFacilityWithOrderingFacilityIdentifier
        implements CustomFhirTransformation {

    @Override
    public void transform(HealthData<?> resource, Map<String, Object> args) {
        Bundle bundle = (Bundle) resource.getUnderlyingData();
        DiagnosticReport diagnosticReport = HapiHelper.getDiagnosticReport(bundle);
        if (diagnosticReport == null) {
            return;
        }
        ServiceRequest serviceRequest = HapiHelper.getServiceRequest(diagnosticReport);
        if (serviceRequest == null) {
            return;
        }
        String orc21_10 = HapiHelper.getORC21Value(serviceRequest);
        HapiHelper.setMSH6_1Value(bundle, orc21_10);
        HapiHelper.removeMSH6_2_and_3_Identifier(bundle);
    }
}
