package gov.hhs.cdc.trustedintermediary

import gov.hhs.cdc.trustedintermediary.etor.results.Result

/**
 * A mock implementation of the {@link gov.hhs.cdc.trustedintermediary.etor.results.Result} interface that is easy to use in tests.
 */
class ResultMock<T> implements Result<T> {

    private String fhirResourceId
    private T underlyingResult

    ResultMock(String fhirResourceId, T underlyingOrders) {
        this.fhirResourceId = fhirResourceId
        this.underlyingResult = underlyingOrders
    }

    @Override
    T getUnderlyingResult() {
        return underlyingResult
    }

    @Override
    String getFhirResourceId() {
        return fhirResourceId
    }
}
