package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom;

import gov.hhs.cdc.trustedintermediary.etor.ruleengine.FhirResource;
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.CustomFhirTransformation;
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiHelper;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Resource;

public class RemoveObservationByCode implements CustomFhirTransformation {
    public static final String CODE_NAME = "code";
    public static final String CODING_SYSTEM_NAME = "codingSystemExtension";
    public static final String CODING_NAME = "codingExtension";

    @Override
    public void transform(FhirResource<?> resource, Map<String, String> args) {
        var bundle = (Bundle) resource.getUnderlyingResource();
        Set<Resource> resourcesToRemove = new HashSet<>();

        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            Resource resourceEntry = entry.getResource();

            if (resourceEntry instanceof Observation observation) {
                processObservation(observation, resourcesToRemove, args);
            }
        }

        bundle.getEntry().removeIf(entry -> resourcesToRemove.contains(entry.getResource()));
    }

    private void processObservation(
            Observation observation, Set<Resource> resourcesToRemove, Map<String, String> args) {
        for (Coding coding : observation.getCode().getCoding()) {
            if (isMatchingCode(coding, args)) {
                resourcesToRemove.add(observation);
                break; // No need to continue once a match is found
            }
        }
    }

    // TODO: Need to refactor this to handle missing extensions, etc. and determine if there's a way
    // to generalize it
    private Boolean isMatchingCode(Coding coding, Map<String, String> args) {
        return Objects.equals(coding.getCode(), args.get(CODE_NAME))
                && coding.getExtensionByUrl(HapiHelper.EXTENSION_CODING_SYSTEM)
                        .getValue()
                        .toString()
                        .equals(args.get(CODING_SYSTEM_NAME))
                && coding.getExtensionByUrl(HapiHelper.EXTENSION_CWE_CODING)
                        .getValue()
                        .toString()
                        .equals(args.get(CODING_NAME));
    }
}
