package gov.hhs.cdc.trustedintermediary.etor.demographics;

import javax.annotation.Nonnull;

/** Contains an ID that is reflected from the patient demographic data. */
public class PatientDemographicsResponse {

    private String fhirResourceId;
    private String patientId;

    PatientDemographicsResponse(String fhirResourceId, String patientId) {
        setFhirResourceId(fhirResourceId);
        setPatientId(patientId);
    }

    public PatientDemographicsResponse(@Nonnull PatientDemographics patientDemographics) {
        setFhirResourceId(patientDemographics.getFhirResourceId());
        setPatientId(patientDemographics.getPatientId());
    }

    public String getFhirResourceId() {
        return fhirResourceId;
    }

    public void setFhirResourceId(String fhirResourceId) {
        this.fhirResourceId = fhirResourceId;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }
}
