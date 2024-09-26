package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom;

import gov.hhs.cdc.trustedintermediary.etor.ruleengine.FhirResource;
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.CustomFhirTransformation;
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiHelper;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Resource;

public class RemoveAccessionNumber implements CustomFhirTransformation {
    private static final String ACCESSION_NUMBER_CODE = "99717-5";

    @Override
    public void transform(FhirResource<?> resource, Map<String, String> args) {
        var bundle = (Bundle) resource.getUnderlyingResource();
        Set<Resource> resourcesToRemove = new HashSet<>();

        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            Resource resourceEntry = entry.getResource();

            if (resourceEntry instanceof Observation observation) {
                var coding = observation.getCode().getCodingFirstRep();

                if (HapiHelper.hasLocalCodeInAlternateCoding(coding)
                        && Objects.equals(coding.getCode(), ACCESSION_NUMBER_CODE)) {
                    resourcesToRemove.add(observation);
                }
            }
        }

        bundle.getEntry().removeIf(entry -> resourcesToRemove.contains(entry.getResource()));
    }
}
