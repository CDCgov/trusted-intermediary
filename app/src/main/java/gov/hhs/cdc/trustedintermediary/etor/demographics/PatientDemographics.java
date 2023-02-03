package gov.hhs.cdc.trustedintermediary.etor.demographics;

import java.time.ZonedDateTime;
import java.util.StringJoiner;

/** Contains demographic data for a patient. */
public class PatientDemographics {
    private String fhirResourceId;
    private String patientId;
    private String firstName;
    private String lastName;
    private String sex;
    private ZonedDateTime birthDateTime;
    private Integer birthOrder;
    private String race;
    private NextOfKin nextOfKin;

    public PatientDemographics(
            String fhirResourceId,
            String patientId,
            String firstName,
            String lastName,
            String sex,
            ZonedDateTime birthDateTime,
            Integer birthOrder,
            String race,
            NextOfKin nextOfKin) {
        this.fhirResourceId = fhirResourceId;
        this.patientId = patientId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.sex = sex;
        this.birthDateTime = birthDateTime;
        this.birthOrder = birthOrder;
        this.race = race;
        this.nextOfKin = nextOfKin;
    }

    public PatientDemographics() {}

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

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public ZonedDateTime getBirthDateTime() {
        return birthDateTime;
    }

    public void setBirthDateTime(ZonedDateTime birthDateTime) {
        this.birthDateTime = birthDateTime;
    }

    public Integer getBirthOrder() {
        return birthOrder;
    }

    public void setBirthOrder(Integer birthOrder) {
        this.birthOrder = birthOrder;
    }

    public String getRace() {
        return race;
    }

    public void setRace(final String race) {
        this.race = race;
    }

    public NextOfKin getNextOfKin() {
        return nextOfKin;
    }

    public void setNextOfKin(final NextOfKin nextOfKin) {
        this.nextOfKin = nextOfKin;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", PatientDemographics.class.getSimpleName() + "[", "]")
                .add("fhirResourceId='" + fhirResourceId + "'")
                .add("patientId='" + patientId + "'")
                .add("firstName='" + firstName + "'")
                .add("lastName='" + lastName + "'")
                .add("sex='" + sex + "'")
                .add("birthDateTime=" + birthDateTime)
                .add("birthOrder=" + birthOrder)
                .add("race='" + race + "'")
                .add("nextOfKin=" + nextOfKin)
                .toString();
    }
}
