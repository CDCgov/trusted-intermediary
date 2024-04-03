package gov.hhs.cdc.trustedintermediary.external.hapi;

import gov.hhs.cdc.trustedintermediary.context.ApplicationContext;
import gov.hhs.cdc.trustedintermediary.etor.messages.MessageHdDataType;
import gov.hhs.cdc.trustedintermediary.etor.results.Result;
import org.hl7.fhir.r4.model.Bundle;

/** Filler concrete implementation of a {@link Result} using the Hapi FHIR library */
public class HapiResult implements Result<Bundle> {

    private final HapiMessageHelper MESSAGE_HELPER =
            ApplicationContext.getImplementation(HapiMessageHelper.class);

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
        return MESSAGE_HELPER.extractPlacerOrderNumber(innerResult);
    }

    @Override
    public String getSendingApplicationDetails() {
        MessageHdDataType sendingApplicationDetails =
                MESSAGE_HELPER.extractSendingApplicationDetails(innerResult);
        return sendingApplicationDetails.toString();
    }

    @Override
    public String getSendingFacilityDetails() {
        MessageHdDataType sendingFacilityDetails =
                MESSAGE_HELPER.extractSendingFacilityDetails(innerResult);
        return sendingFacilityDetails.toString();
    }

    @Override
    public String getReceivingApplicationDetails() {
        MessageHdDataType receivingApplicationDetails =
                MESSAGE_HELPER.extractReceivingApplicationDetails(innerResult);
        return receivingApplicationDetails.toString();
    }

    @Override
    public String getReceivingFacilityDetails() {
        MessageHdDataType receivingFacilityDetails =
                MESSAGE_HELPER.extractReceivingFacilityDetails(innerResult);
        return receivingFacilityDetails.toString();
    }
}
