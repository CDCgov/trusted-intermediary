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
    public MessageHdDataType getSendingApplicationDetails() {
        String sendingApplicationNamespace =
                MESSAGE_HELPER.extractSendingApplicationNamespace(innerResult);
        String sendingApplicationUniversalId =
                MESSAGE_HELPER.extractSendingApplicationUniversalId(innerResult);
        String sendingApplicationUniversalIdType =
                MESSAGE_HELPER.extractSendingApplicationUniversalIdType(innerResult);

        return new MessageHdDataType(
                sendingApplicationNamespace,
                sendingApplicationUniversalId,
                sendingApplicationUniversalIdType);
    }

    @Override
    public MessageHdDataType getSendingFacilityDetails() {
        String sendingFacilityNamespace =
                MESSAGE_HELPER.extractSendingFacilityNamespace(innerResult);
        String sendingFacilityUniversalId =
                MESSAGE_HELPER.extractSendingFacilityUniversalId(innerResult);
        String sendingFacilityUniversalIdType =
                MESSAGE_HELPER.extractSendingFacilityUniversalIdType(innerResult);

        return new MessageHdDataType(
                sendingFacilityNamespace,
                sendingFacilityUniversalId,
                sendingFacilityUniversalIdType);
    }

    @Override
    public MessageHdDataType getReceivingApplicationDetails() {
        String receivingApplicationNamespace =
                MESSAGE_HELPER.extractReceivingApplicationNamespace(innerResult);
        String receivingApplicationUniversalId =
                MESSAGE_HELPER.extractReceivingApplicationUniversalId(innerResult);
        String receivingApplicationUniversalIdType =
                MESSAGE_HELPER.extractReceivingApplicationUniversalIdType(innerResult);

        return new MessageHdDataType(
                receivingApplicationNamespace,
                receivingApplicationUniversalId,
                receivingApplicationUniversalIdType);
    }

    @Override
    public MessageHdDataType getReceivingFacilityDetails() {
        String receivingFacilityNamespace =
                MESSAGE_HELPER.extractReceivingFacilityNamespace(innerResult);
        String receivingFacilityUniversalId =
                MESSAGE_HELPER.extractReceivingFacilityUniversalId(innerResult);
        String receivingFacilityUniversalIdType =
                MESSAGE_HELPER.extractReceivingFacilityUniversalIdType(innerResult);

        return new MessageHdDataType(
                receivingFacilityNamespace,
                receivingFacilityUniversalId,
                receivingFacilityUniversalIdType);
    }
}
