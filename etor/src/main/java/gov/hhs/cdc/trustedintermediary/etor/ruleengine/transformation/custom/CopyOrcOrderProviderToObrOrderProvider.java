package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom;

import gov.hhs.cdc.trustedintermediary.etor.ruleengine.FhirResource;
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.CustomFhirTransformation;
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiHelper;
import java.util.Map;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.hl7.fhir.r4.model.ServiceRequest;

/** Updates the order provider (OBR-16) from the order provider (ORC-12) */
public class CopyOrcOrderProviderToObrOrderProvider implements CustomFhirTransformation {

    @Override
    public void transform(FhirResource<?> resource, Map<String, String> args) {
        Bundle bundle = (Bundle) resource.getUnderlyingResource();
        DiagnosticReport diagnosticReport = HapiHelper.getDiagnosticReport(bundle);
        if (diagnosticReport == null) {
            return;
        }
        ServiceRequest serviceRequest = HapiHelper.getServiceRequest(diagnosticReport);
        if (serviceRequest == null) {
            return;
        }
        var pract = HapiHelper.getPractitionerRole(serviceRequest);
        var toOverwrite =
                serviceRequest
                        .getExtensionByUrl(HapiHelper.EXTENSION_OBR_URL)
                        .getExtensionByUrl(HapiHelper.EXTENSION_OBR16_DATA_TYPE.toString());
        toOverwrite.setValue(pract.getPractitioner());
    }
}
