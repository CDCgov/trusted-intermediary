package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom;

import gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.CustomFhirTransformation;
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiHelper;
import gov.hhs.cdc.trustedintermediary.wrappers.HealthData;
import java.util.Map;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.ServiceRequest;

/** Updates the order provider (OBR-16) from the order provider (ORC-12) */
public class CopyOrcOrderProviderToObrOrderProvider implements CustomFhirTransformation {

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
        var practitionerRole = HapiHelper.getPractitionerRole(serviceRequest);
        if (practitionerRole == null) {
            return;
        }

        // Extract or create the OBR-16 extension from the ServiceRequest
        Extension obrExtension =
                HapiHelper.ensureExtensionExists(serviceRequest, HapiHelper.EXTENSION_OBR_URL);

        // Extract or create the OBR-16 data type extension
        Extension obr16Extension =
                HapiHelper.ensureSubExtensionExists(
                        obrExtension, HapiHelper.EXTENSION_OBR16_DATA_TYPE.toString());

        // Set the ORC-12 Practitioner in the OBR-16 extension
        HapiHelper.setOBR16WithPractitioner(obr16Extension, practitionerRole);
    }
}
