package gov.hhs.cdc.trustedintermediary.etor.demographics;

import javax.annotation.Nonnull;

/** Contains an ID that is reflected from the patient demographic data. */
public class PatientDemographicsResponse {

    private String id;

    public PatientDemographicsResponse(String id) {
        setId(id);
    }

    public PatientDemographicsResponse(@Nonnull PatientDemographics patientDemographics) {
        setId(patientDemographics.getRequestId());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
