package gov.hhs.cdc.trustedintermediary

import gov.hhs.cdc.trustedintermediary.etor.results.Result

/**
 * A mock implementation of the {@link gov.hhs.cdc.trustedintermediary.etor.results.Result} interface that is easy to use in tests.
 */
class ResultMock<T> implements Result<T> {

    private String fhirResourceId
    private T underlyingResult
    private String placerOrderNumber
    private String sendingApplicationId
    private String sendingFacilityId
    private String receivingApplicationId
    private String receivingFacilityId


    ResultMock(String fhirResourceId, T underlyingResult, String placerOrderNumber, String sendingApplicationId, String sendingFacilityId,
    String receivingApplicationId, String receivingFacilityId) {
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
    String getSendingApplicationDetails() {
        return this.sendingApplicationDetails
    }

    @Override
    String getSendingFacilityDetails() {
        return this.sendingFacilityDetails
    }

    @Override
    String getReceivingApplicationDetails() {
        return this.receivingApplicationDetails
    }

    @Override
    String getReceivingFacilityDetails() {
        return this.receivingFacilityDetails
    }
}
