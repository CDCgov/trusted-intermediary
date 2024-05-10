package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom;

import gov.hhs.cdc.trustedintermediary.etor.ruleengine.FhirResource;
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.CustomFhirTransformation;
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiHelper;
import java.util.Map;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Patient;

public class scrubPatientIdentifiers implements CustomFhirTransformation {

    @Override
    public void transform(FhirResource<?> resource, Map<String, String> args) {
        Bundle bundle = (Bundle) resource.getUnderlyingResource();
        Patient patient = (Patient) HapiHelper.resourceInBundle(bundle, Patient.class);
        Identifier patientIdentifier = patient.getIdentifierFirstRep();
        Organization organization = (Organization) patientIdentifier.getAssigner().getResource();
        organization.getIdentifierFirstRep().setValue(""); // remove PID.3-4
        patientIdentifier.getType().getCodingFirstRep().setCode(""); // remove PID.3-5
    }
}
