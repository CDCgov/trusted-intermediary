package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom;

import gov.hhs.cdc.trustedintermediary.context.ApplicationContext;
import gov.hhs.cdc.trustedintermediary.etor.metadata.EtorMetadataStep;
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.FhirResource;
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.CustomFhirTransformation;
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiHelper;
import gov.hhs.cdc.trustedintermediary.wrappers.MetricMetadata;
import java.util.Map;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.ServiceRequest;

public class switchPlacerOrderAndGroupNumbers implements CustomFhirTransformation {

    private final MetricMetadata metadata =
            ApplicationContext.getImplementation(MetricMetadata.class);

    @Override
    public void transform(FhirResource<?> resource, Map<String, String> args) {
        //        Update ORC-2 with content from ORC-4 in the ORU result message.
        //                Replace ORC-2.1 with content of ORC-4.1.
        //                Replace ORC-2.2 with content of ORC-4.2
        //        Effectively, we're swapping ORC-2 for ORC-4 and vice versa
        //        OBR 2.1: identifier[0]. value
        //        OBR 2.2: identifier.extension[1].extension[0].valueString
        //        OBR 4.1: code.coding[0].code
        //        OBR 4.2: code.coding[0].display
        Bundle bundle = (Bundle) resource.getUnderlyingResource();
        var serviceRequests = HapiHelper.resourcesInBundle(bundle, ServiceRequest.class);

        serviceRequests.forEach(
                serviceRequest -> {
                    var twoPointOne = serviceRequest.getIdentifier().get(0);
                    var twoPointTwo =
                            serviceRequest
                                    .getIdentifier()
                                    .get(0)
                                    .getExtensionByUrl(
                                            "https://reportstream.cdc.gov/fhir/StructureDefinition/namespace-id");
                    var fourPointOne = serviceRequest.getCode().getCoding().get(0);
                    var fourPointTwo = serviceRequest.getCode().getCoding().get(0);

                    var valueHolder = twoPointOne.getValue();
                    twoPointOne.setValue(fourPointOne.getCode());
                    fourPointOne.setCode(valueHolder);

                    valueHolder = twoPointTwo.getValue().primitiveValue();
                    twoPointTwo.setValue(fourPointTwo.getDisplayElement());
                    fourPointTwo.setCode(valueHolder);
                });
        metadata.put(bundle.getId(), EtorMetadataStep.SWITCH_PLACER_ORDER_AND_GROUP_NUMBERS);
    }
}
