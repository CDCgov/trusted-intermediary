package gov.hhs.cdc.trustedintermediary.external.hapi;

import gov.hhs.cdc.trustedintermediary.etor.orders.Task;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Patient;

public class HapiTask implements Task<Bundle> {

    private final Bundle innerTask;

    public HapiTask(Bundle innerOrder) {
        this.innerTask = innerOrder;
    }

    @Override
    public Bundle getUnderlyingTask() {
        return innerTask;
    }

    @Override
    public String getFhirResourceId() {
        return innerTask.getId();
    }

    @Override
    public String getServiceRequestId() {
        return HapiHelper.resourcesInBundle(innerTask, Patient.class)
                .flatMap(patient -> patient.getIdentifier().stream())
                .filter(
                        identifier ->
                                identifier
                                        .getType()
                                        .hasCoding(
                                                "http://terminology.hl7.org/CodeSystem/v2-0203",
                                                "MR"))
                .map(Identifier::getValue)
                .findFirst()
                .orElse("");
    }

    @Override
    public String getSpecimenId() {
        return HapiHelper.resourcesInBundle(innerTask, Patient.class)
                .flatMap(patient -> patient.getIdentifier().stream())
                .filter(
                        identifier ->
                                identifier
                                        .getType()
                                        .hasCoding(
                                                "http://terminology.hl7.org/CodeSystem/v2-0203",
                                                "MR"))
                .map(Identifier::getValue)
                .findFirst()
                .orElse("");
    }
}
