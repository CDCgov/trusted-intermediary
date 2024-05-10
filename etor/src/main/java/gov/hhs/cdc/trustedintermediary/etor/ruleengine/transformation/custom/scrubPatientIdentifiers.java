package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom;

import gov.hhs.cdc.trustedintermediary.etor.ruleengine.FhirResource;
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.CustomFhirTransformation;
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiHelper;
import java.util.Map;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Patient;

public class scrubPatientIdentifiers implements CustomFhirTransformation {

    @Override
    public void transform(FhirResource<?> resource, Map<String, String> args) {
        // Remove 3.4, 3.5
        // Strip content from PID3
        // Bundle.entry.resource.ofType(Patient).identifier.assigner.resolve().identifier.value

        Bundle bundle = (Bundle) resource.getUnderlyingResource();
        Patient patient = (Patient) HapiHelper.resourceInBundle(bundle, Patient.class);
        var identifier = patient.getIdentifier().get(0).getAssigner().getIdentifier().getValue();
    }
}
