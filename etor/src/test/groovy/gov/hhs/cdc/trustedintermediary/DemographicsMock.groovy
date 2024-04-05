package gov.hhs.cdc.trustedintermediary

import gov.hhs.cdc.trustedintermediary.etor.demographics.Demographics

/**
 * A mock implementation of the {@link Demographics} interface that is easy to use in tests.
 */
class DemographicsMock<T> implements Demographics<T> {

    private String fhirResourceId
    private String patientId
    private T underlyingDemographics


    DemographicsMock(String fhirResourceId, String patientId, T underlyingDemographics) {
        this.fhirResourceId = fhirResourceId
        this.patientId = patientId
        this.underlyingDemographics = underlyingDemographics
    }

    @Override
    T getUnderlyingResource() {
        return underlyingDemographics
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
