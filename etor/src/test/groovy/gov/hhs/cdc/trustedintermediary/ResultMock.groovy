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
    private MessageHdDataType sendingApplicationDetails
    private MessageHdDataType sendingFacilityDetails
    private MessageHdDataType receivingApplicationDetails
    private MessageHdDataType receivingFacilityDetails


    ResultMock(String fhirResourceId, T underlyingResult, String placerOrderNumber, MessageHdDataType sendingApplicationDetails, MessageHdDataType sendingFacilityDetails,
    MessageHdDataType receivingApplicationDetails, MessageHdDataType receivingFacilityDetails) {
        this.fhirResourceId = fhirResourceId
        this.underlyingResult = underlyingResult
        this.placerOrderNumber = placerOrderNumber
        this.sendingApplicationDetails = sendingApplicationDetails
        this.sendingFacilityDetails = sendingFacilityDetails
        this.receivingApplicationDetails = receivingApplicationDetails
        this.receivingFacilityDetails = receivingFacilityDetails
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
