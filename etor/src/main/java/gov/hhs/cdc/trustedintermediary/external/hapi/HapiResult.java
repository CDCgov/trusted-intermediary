package gov.hhs.cdc.trustedintermediary.external.hapi;

import gov.hhs.cdc.trustedintermediary.etor.results.Result;
import org.hl7.fhir.r4.model.Bundle;

/** Filler concrete implementation of a {@link Result} using the Hapi FHIR library */
public class HapiResult implements Result<Bundle> {

    private final Bundle innerResult;

    public HapiResult(Bundle innerResult) {
        this.innerResult = innerResult;
    }

    @Override
    public Bundle getUnderlyingResult() {
        return innerResult;
    }

    @Override
    public String getFhirResourceId() {
        return innerResult.getId();
    }

    @Override
    public String getPlacerOrderNumber() {
        return null;
    }

    @Override
    public String getSendingApplicationId() {
        return null;
    }

    @Override
    public String getSendingFacilityId() {
        return null;
    }

    @Override
    public String getReceivingApplicationId() {
        return null;
    }

    @Override
    public String getReceivingFacilityId() {
        return null;
    }
}
