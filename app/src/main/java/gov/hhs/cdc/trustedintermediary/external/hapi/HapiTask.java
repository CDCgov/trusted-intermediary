package gov.hhs.cdc.trustedintermediary.external.hapi;

import gov.hhs.cdc.trustedintermediary.etor.orders.Task;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;

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
    public String getTaskId() {
        return HapiHelper.resourcesInBundle(innerTask, org.hl7.fhir.r4.model.Task.class)
                .map(Resource::getId)
                .findFirst()
                .orElse("");
    }

    @Override
    public String getServiceRequestId() {
        return HapiHelper.resourcesInBundle(innerTask, org.hl7.fhir.r4.model.Task.class)
                .map(task -> task.getFocus().getReference())
                .findFirst()
                .orElse("");
    }

    @Override
    public String getSpecimenId() {
        return HapiHelper.resourcesInBundle(innerTask, org.hl7.fhir.r4.model.Task.class)
                .flatMap(task -> task.getOutput().stream())
                .filter(output -> output.getValue().hasType("Reference"))
                .map(output -> output.getValue().castToReference(output.getValue()))
                .map(Reference::getReference)
                .filter(reference -> reference.startsWith("Specimen/"))
                .findFirst()
                .orElse("");
    }
}
