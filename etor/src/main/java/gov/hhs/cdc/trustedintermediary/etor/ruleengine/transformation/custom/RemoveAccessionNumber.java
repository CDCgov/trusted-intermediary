package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom;

import gov.hhs.cdc.trustedintermediary.etor.ruleengine.FhirResource;
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.CustomFhirTransformation;
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiHelper;
import java.util.Map;
import java.util.Objects;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Observation;

public class RemoveAccessionNumber implements CustomFhirTransformation {
    private static final String ACCESSION_NUMBER_CODE = "99717-5";

    @Override
    public void transform(FhirResource<?> resource, Map<String, String> args) {
        var bundle = (Bundle) resource.getUnderlyingResource();
        var observations = HapiHelper.resourcesInBundle(bundle, Observation.class);

        for (Observation obv : observations.toList()) {
            var codingList = obv.getCode().getCoding();

            if (codingList.size() != 1) {
                continue;
            }

            var coding = codingList.get(0);

            if (HapiHelper.hasLocalCodeInAlternateCoding(coding)
                    && Objects.equals(coding.getCode(), ACCESSION_NUMBER_CODE)) {
                codingList.clear();
            }
        }
    }
}
