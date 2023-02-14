package gov.hhs.cdc.trustedintermediary.external.hapi;

import gov.hhs.cdc.trustedintermediary.etor.demographics.LabOrder;
import org.hl7.fhir.r4.model.Bundle;

public class HapiLabOrder implements LabOrder<Bundle> {

    private final Bundle innerLabOrder;

    public HapiLabOrder(Bundle innerLabOrder) {
        this.innerLabOrder = innerLabOrder;
    }

    @Override
    public Bundle getUnderlyingOrder() {
        return innerLabOrder;
    }
}
