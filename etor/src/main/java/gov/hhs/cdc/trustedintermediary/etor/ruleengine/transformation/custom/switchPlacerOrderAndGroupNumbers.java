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

    //    private final MetricMetadata metadata =
    //            ApplicationContext.getImplementation(MetricMetadata.class);

    @Override
    public void transform(FhirResource<?> resource, Map<String, String> args) {
        // OBR 2.1: identifier[0]. value
        // OBR 2.2: identifier.extension[1].extension[0].valueString
        // OBR 4.1: code.coding[0].code
        // OBR 4.2: code.coding[0].display
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
                    var obr2_1 = serviceIdentifier.getValue();
                    var obr2_2 = serviceNamespaceExtension.getValue().primitiveValue();
                    var obr4_1 = serviceCoding.getCode();
                    var obr4_2 = serviceCoding.getDisplay();

                    // Switch values between OBR 2.1 and 4.1
                    serviceIdentifier.setValue(obr4_1);
                    serviceCoding.setCode(obr2_1);

                    // Switch values between OBR 2.2 and 4.2
                    serviceNamespaceExtension.setValue(new StringType(obr4_2));
                    serviceCoding.setDisplay(obr2_2);
                });
        //        metadata.put(bundle.getId(),
        // EtorMetadataStep.SWITCH_PLACER_ORDER_AND_GROUP_NUMBERS);
    }
}
