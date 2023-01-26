package gov.hhs.cdc.trustedintermediary.etor.demographics;

import java.time.ZonedDateTime;
import javax.annotation.Nonnull;

/** Contains an ID that is reflected from the patient demographic data. */
public class PatientDemographicsResponse {

    private String id;
    private String firstName;
    private String lastName;
    private String sex;
    private ZonedDateTime birthDateTime;
    private Integer birthOrder;

    public PatientDemographicsResponse(String id) {
        setId(id);
    }

    public PatientDemographicsResponse(@Nonnull PatientDemographics patientDemographics) {
        setId(patientDemographics.getRequestId());
        setFirstName(patientDemographics.getFirstName());
        setLastName(patientDemographics.getLastName());
        setSex(patientDemographics.getSex());
        //        setBirthDateTime(patientDemographics.getBirthDateTime());
        setBirthOrder(patientDemographics.getBirthOrder());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
}
