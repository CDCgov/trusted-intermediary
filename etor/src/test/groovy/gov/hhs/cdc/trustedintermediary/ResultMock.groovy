package gov.hhs.cdc.trustedintermediary

import gov.hhs.cdc.trustedintermediary.etor.results.Result

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
