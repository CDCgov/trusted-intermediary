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

        // Get values

        // ORC 12.1 - id # XCN.1
        String orc12_1 = HapiHelper.getORC12_1Value(serviceRequest);

        var ref = serviceRequest.getRequester();
        var pract = HapiHelper.getPractitionerRole(serviceRequest);

        //        def obr16Practitioner = getObr16ExtensionPractitioner(serviceRequest)
        //        def obr16XcnExtension =
        // obr16Practitioner.getExtensionByUrl(PRACTITIONER_EXTENSION_URL)

        var toOverwrite =
                serviceRequest
                        .getExtensionByUrl(HapiHelper.EXTENSION_OBR_URL)
                        .getExtensionByUrl(HapiHelper.EXTENSION_OBR16_DATA_TYPE.toString());

        //        toOverwrite.setValue(ref);
        toOverwrite.setValue(pract.getPractitioner());

        //        return serviceRequest
        //                .getExtensionByUrl(HapiHelper.EXTENSION_OBR_URL)
        //                .getExtensionByUrl(HapiHelper.EXTENSION_OBR16_DATA_TYPE.toString())
        //                .value
        //                .getResource()

        //        String orc21_10 = HapiHelper.getORC21Value(serviceRequest);
        //        HapiHelper.setMSH6_1Value(bundle, orc21_10);
        //        HapiHelper.removeMSH6_2_and_3_Identifier(bundle);

        //        ORC 12.2 - family name XCN.2
        //        ORC 12.3 - given name XCN.3
        //        ORC 12.10 - NPI XCN.10

        // Set values in OBR-16
    }
}
