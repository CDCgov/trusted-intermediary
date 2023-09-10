package gov.hhs.cdc.trustedintermediary.external.hapi;

import gov.hhs.cdc.trustedintermediary.etor.tasks.Task;
import org.hl7.fhir.r4.model.Reference;

public class HapiTask implements Task<org.hl7.fhir.r4.model.Task> {

    private final org.hl7.fhir.r4.model.Task innerTask;

    public HapiTask(org.hl7.fhir.r4.model.Task innerOrder) {
        this.innerTask = innerOrder;
    }

    @Override
    public org.hl7.fhir.r4.model.Task getUnderlyingTask() {
        return innerTask;
    }

    @Override
    public String getTaskId() {
        return innerTask.getId();
    }

    @Override
    public String getServiceRequestId() {
        return innerTask.getFocus().getReference();
    }

    @Override
    public String getSpecimenId() {
        return innerTask.getOutput().stream()
                .filter(output -> output.getValue().hasType("Reference"))
                .map(output -> output.getValue().castToReference(output.getValue()))
                .map(Reference::getReference)
                .filter(reference -> reference.startsWith("Specimen/"))
                .findFirst()
                .orElse("");
    }
}
