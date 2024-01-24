package gov.hhs.cdc.trustedintermediary

import gov.hhs.cdc.trustedintermediary.etor.results.Result

/**
 * A mock implementation of the {@link gov.hhs.cdc.trustedintermediary.etor.results.Result} interface that is easy to use in tests.
 */
class ResultMock<T> implements Result<T> {

    private String fhirResourceId
    private String patientId
    private T underlyingResult

    ResultMock(String fhirResourceId, String patientId, T underlyingOrders) {
        this.fhirResourceId = fhirResourceId
        this.patientId = patientId
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

    @Override
    String getPatientId() {
        return patientId
    }
}
