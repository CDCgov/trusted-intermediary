package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom;

import gov.hhs.cdc.trustedintermediary.etor.ruleengine.FhirResource;
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.CustomFhirTransformation;
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiHelper;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.ServiceRequest;

/**
 * This transformation removes all OBRs from an ORU message except for the OBR with a given value in
 * OBR-4.1. All OBXs are attached to the sole remaining OBR
 */
public class RemoveObservationRequests implements CustomFhirTransformation {

    @Override
    public void transform(FhirResource<?> resource, Map<String, String> args) {
        Bundle bundle = (Bundle) resource.getUnderlyingResource();

        String universalServiceIdentifier = args.get("universalServiceIdentifier");
        Set<Resource> resourcesToRemove = new HashSet<>();
        List<Reference> observationReferences = new ArrayList<>();
        DiagnosticReport singleDiagnosticReport = null;
        ServiceRequest singleServiceRequest = null;

        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            Resource resourceEntry = entry.getResource();
            if (resourceEntry instanceof Observation observation) {
                observationReferences.add(new Reference(observation.getId()));
            } else if (resourceEntry instanceof DiagnosticReport diagnosticReport) {
                resourcesToRemove.add(diagnosticReport);
                ServiceRequest serviceRequest = HapiHelper.getServiceRequest(diagnosticReport);
                if (serviceRequest != null) {
                    String obr4_1 = HapiHelper.getOBR4_1Value(serviceRequest);
                    if (universalServiceIdentifier.equals(obr4_1)) {
                        singleDiagnosticReport = diagnosticReport;
                        singleServiceRequest = serviceRequest;
                    }
                }

            } else if (resourceEntry instanceof ServiceRequest serviceRequest) {
                resourcesToRemove.add(serviceRequest);
            }
        }

        if (singleDiagnosticReport != null) {
            bundle.getEntry().removeIf(entry -> resourcesToRemove.contains(entry.getResource()));
            bundle.addEntry().setResource(singleDiagnosticReport);
            bundle.addEntry().setResource(singleServiceRequest);
            singleDiagnosticReport.setResult(observationReferences);
        }
    }
}
