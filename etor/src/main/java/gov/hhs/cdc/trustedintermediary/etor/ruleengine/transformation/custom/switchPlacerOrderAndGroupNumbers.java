package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom;

import gov.hhs.cdc.trustedintermediary.etor.ruleengine.FhirResource;
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.CustomFhirTransformation;
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiHelper;
import java.util.Map;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.ServiceRequest;
import org.hl7.fhir.r4.model.StringType;

/**
 * Updates Placer Order Number (ORC-2) with content from Placer Group Number (ORC-4). It also
 * replaces Placer Order Number's Entity Identifier (ORC-2.1) and Namespace Id (ORC-2.2) with Placer
 * Group Number's Entity Identifier (ORC-4.1) and Namespace Id (ORC-4.2) respectively. Effectively,
 * we're swapping ORC-2 for ORC-4 and vice versa.
 */
public class switchPlacerOrderAndGroupNumbers implements CustomFhirTransformation {
    @Override
    public void transform(FhirResource<?> resource, Map<String, String> args) {
        Bundle bundle = (Bundle) resource.getUnderlyingResource();
        var serviceRequests = HapiHelper.resourcesInBundle(bundle, ServiceRequest.class);

        serviceRequests.forEach(
                serviceRequest -> {
                    var serviceIdentifier = serviceRequest.getIdentifier().get(0);
                    var serviceNamespaceExtension =
                            serviceRequest
                                    .getIdentifier()
                                    .get(0)
                                    .getExtensionByUrl(
                                            "https://reportstream.cdc.gov/fhir/StructureDefinition/assigning-authority")
                                    .getExtensionByUrl(
                                            "https://reportstream.cdc.gov/fhir/StructureDefinition/namespace-id");
                    var serviceCoding = serviceRequest.getCode().getCoding().get(0);
                    var orc2_1 = serviceIdentifier.getValue();
                    var orc2_2 = serviceNamespaceExtension.getValue().primitiveValue();
                    var orc4_1 = serviceCoding.getCode();
                    var orc4_2 = serviceCoding.getDisplay();

                    // Switch values between ORC 2.1 and 4.1
                    serviceIdentifier.setValue(orc4_1);
                    serviceCoding.setCode(orc2_1);

                    // Switch values between ORC 2.2 and 4.2
                    serviceNamespaceExtension.setValue(new StringType(orc4_2));
                    serviceCoding.setDisplay(orc2_2);
                });
    }
}
