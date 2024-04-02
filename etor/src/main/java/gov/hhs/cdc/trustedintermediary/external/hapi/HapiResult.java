package gov.hhs.cdc.trustedintermediary.external.hapi;

import gov.hhs.cdc.trustedintermediary.etor.messages.MessageHdDataType;
import gov.hhs.cdc.trustedintermediary.etor.results.Result;
import javax.inject.Inject;
import org.hl7.fhir.r4.model.Bundle;

/** Filler concrete implementation of a {@link Result} using the Hapi FHIR library */
public class HapiResult implements Result<Bundle> {

    @Inject HapiMessageHelper hapiMessageHelper;

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
        return HapiMessageHelper.getInstance().extractPlacerOrderNumber(innerResult);
    }

    @Override
    public String getSendingApplicationDetails() {
        MessageHdDataType sendingApplicationDetails =
                HapiMessageHelper.getInstance().extractSendingApplicationDetails(innerResult);
        return sendingApplicationDetails.toString();
    }

    @Override
    public String getSendingFacilityDetails() {
        MessageHdDataType sendingFacilityDetails =
                HapiMessageHelper.getInstance().extractSendingFacilityDetails(innerResult);
        return sendingFacilityDetails.toString();
    }

    @Override
    public String getReceivingApplicationDetails() {
        MessageHdDataType receivingApplicationDetails =
                HapiMessageHelper.getInstance().extractReceivingApplicationDetails(innerResult);
        return receivingApplicationDetails.toString();
    }

    @Override
    public String getReceivingFacilityDetails() {
        MessageHdDataType receivingFacilityDetails =
                HapiMessageHelper.getInstance().extractReceivingFacilityDetails(innerResult);
        return receivingFacilityDetails.toString();
    }
}
