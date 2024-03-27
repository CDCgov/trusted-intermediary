package gov.hhs.cdc.trustedintermediary

import gov.hhs.cdc.trustedintermediary.etor.results.Result

/**
 * A mock implementation of the {@link gov.hhs.cdc.trustedintermediary.etor.results.Result} interface that is easy to use in tests.
 */
class ResultMock<T> implements Result<T> {

    private String fhirResourceId
    private T underlyingResult

    ResultMock(String fhirResourceId, T underlyingResult) {
        this.fhirResourceId = fhirResourceId
        this.underlyingResult = underlyingResult
    }

    @Override
    T getUnderlyingResult() {
        return underlyingResult
    }

    @Override
    String getFhirResourceId() {
        return fhirResourceId
    }

    @Override
    String getPlacerOrderNumber() {
        return null
    }

    @Override
    String getSendingApplicationId() {
        return null
    }

    @Override
    String getSendingFacilityId() {
        return null
    }

    @Override
    String getReceivingApplicationId() {
        return null
    }

    @Override
    String getReceivingFacilityId() {
        return null
    }
}
