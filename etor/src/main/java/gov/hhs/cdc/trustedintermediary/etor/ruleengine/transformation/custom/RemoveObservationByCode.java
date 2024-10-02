package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom;

import gov.hhs.cdc.trustedintermediary.etor.ruleengine.FhirResource;
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.CustomFhirTransformation;
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiHelper;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Resource;

public class RemoveObservationByCode implements CustomFhirTransformation {
    public static final String CODE_NAME = "code";
    public static final String CODING_SYSTEM_NAME = "codingSystemExtension";
    public static final String CODING_NAME = "codingExtension";

    @Override
    public void transform(FhirResource<?> resource, Map<String, Object> args) {
        var bundle = (Bundle) resource.getUnderlyingResource();
        Set<Resource> resourcesToRemove = new HashSet<>();

        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            Resource resourceEntry = entry.getResource();

            if (!(resourceEntry instanceof Observation observation)) {
                continue;
            }

            if (HapiHelper.hasMatchingCoding(
                    observation,
                    args.get(CODE_NAME).toString(),
                    args.get(CODING_NAME).toString(),
                    args.get(CODING_SYSTEM_NAME).toString())) {
                resourcesToRemove.add(resourceEntry);
            }
        }

        bundle.getEntry().removeIf(entry -> resourcesToRemove.contains(entry.getResource()));
    }
}
