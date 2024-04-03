package gov.hhs.cdc.trustedintermediary

import gov.hhs.cdc.trustedintermediary.etor.messages.MessageHdDataType
import gov.hhs.cdc.trustedintermediary.etor.results.Result

/**
 * A mock implementation of the {@link gov.hhs.cdc.trustedintermediary.etor.results.Result} interface that is easy to use in tests.
 */
class ResultMock<T> implements Result<T> {

    private String fhirResourceId
    private T underlyingResult
    private String placerOrderNumber
    private MessageHdDataType sendingApplicationId
    private MessageHdDataType sendingFacilityId
    private MessageHdDataType receivingApplicationId
    private String receivingFacilityId


    ResultMock(String fhirResourceId, T underlyingResult, String placerOrderNumber, MessageHdDataType sendingApplicationId, MessageHdDataType sendingFacilityId,
    MessageHdDataType receivingApplicationId, MessageHdDataType receivingFacilityId) {
        this.fhirResourceId = fhirResourceId
        this.underlyingResult = underlyingResult
        this.placerOrderNumber = placerOrderNumber
        this.sendingApplicationId = sendingApplicationId
        this.sendingFacilityId = sendingFacilityId
        this.receivingApplicationId = receivingApplicationId
        this.receivingFacilityId = receivingFacilityId
    }

    @Override
    T getUnderlyingResult() {
        return this.underlyingResult
    }

    @Override
    String getFhirResourceId() {
        return this.fhirResourceId
    }

    @Override
    String getPlacerOrderNumber() {
        return this.placerOrderNumber
    }

    @Override
    MessageHdDataType getSendingApplicationDetails() {
        return this.sendingApplicationDetails
    }

    @Override
    MessageHdDataType getSendingFacilityDetails() {
        return this.sendingFacilityDetails
    }

    @Override
    MessageHdDataType getReceivingApplicationDetails() {
        return this.receivingApplicationDetails
    }

    @Override
    MessageHdDataType getReceivingFacilityDetails() {
        return this.receivingFacilityDetails
    }
}
