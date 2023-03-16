package gov.hhs.cdc.trustedintermediary


import gov.hhs.cdc.trustedintermediary.etor.demographics.Demographics

class DemographicsMock implements Demographics<String> {

    private String fhirResourceId
    private String patientId


    DemographicsMock(String fhirResourceId, String patientId) {
        this.fhirResourceId = fhirResourceId
        this.patientId = patientId
    }

    @Override
    String getUnderlyingDemographics() {
        return "Underlying Demographics"
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
