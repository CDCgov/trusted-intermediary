package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom;

import gov.hhs.cdc.trustedintermediary.etor.ruleengine.FhirResource;
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.RuleExecutionException;
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.CustomFhirTransformation;
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiHelper;
import java.util.Map;
import java.util.Objects;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.ServiceRequest;
import org.hl7.fhir.r4.model.StringType;

public class ObrOverrides implements CustomFhirTransformation {
    @Override
    public void transform(FhirResource<?> resource, Map<String, String> args)
            throws RuleExecutionException {
        try {
            Bundle bundle = (Bundle) resource.getUnderlyingResource();
            var serviceRequests = HapiHelper.resourcesInBundle(bundle, ServiceRequest.class);

            serviceRequests.forEach(
                    serviceRequest -> {
                        var serviceCoding = serviceRequest.getCode().getCoding().get(0);
                        if (Objects.equals(serviceCoding.getCode(), args.get("checkValue"))) {
                            var codingSystem =
                                    serviceCoding.getExtensionByUrl(
                                            HapiHelper.EXTENSION_CODING_SYSTEM);
                            codingSystem.setValue(new StringType(args.get("codingSystem")));

                            var altId =
                                    serviceCoding.getExtensionByUrl(
                                            HapiHelper.EXTENSION_ALTERNATE_VALUE);
                            if (altId.isEmpty()) {
                                altId = new Extension();
                                altId.setUrl(HapiHelper.EXTENSION_ALTERNATE_VALUE);
                                serviceCoding.addExtension(altId);
                            }
                            altId.setValue(new StringType(args.get("alternateId")));
                        }
                    });
        } catch (Exception e) {
            throw new RuleExecutionException("Failed to override OBR values", e);
        }
    }
}
