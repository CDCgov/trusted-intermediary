package gov.hhs.cdc.trustedintermediary.etor.operationoutcomes;

import org.hl7.fhir.r4.model.OperationOutcome;

public class HapiFhirMetadata implements FhirMetadata<OperationOutcome> {

    private final OperationOutcome innerOutcome;

    public HapiFhirMetadata(OperationOutcome outcome) {
        this.innerOutcome = outcome;
    }

    @Override
    public OperationOutcome getUnderlyingOutcome() {
        return innerOutcome;
    }
}
