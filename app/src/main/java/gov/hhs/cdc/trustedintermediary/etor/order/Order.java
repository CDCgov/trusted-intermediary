package gov.hhs.cdc.trustedintermediary.etor.order;

import java.time.ZonedDateTime;
import java.util.StringJoiner;

/** Represents an ETOR order. */
public class Order {
    private String requestId;
    private String patientId;
    private String firstName;
    private String lastName;
    private String sex;
    private ZonedDateTime birthDateTime;
    private Integer birthOrder;

    public Order(
            String requestId,
            String patientId,
            String firstName,
            String lastName,
            String sex,
            ZonedDateTime birthDateTime,
            Integer birthOrder) {
        this.requestId = requestId;
        this.patientId = patientId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.sex = sex;
        this.birthDateTime = birthDateTime;
        this.birthOrder = birthOrder;
    }

    public Order() {}

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
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

    @Override
    public String toString() {
        return new StringJoiner(", ", Order.class.getSimpleName() + "[", "]")
                .add("requestId='" + requestId + "'")
                .add("patientId='" + patientId + "'")
                .add("firstName='" + firstName + "'")
                .add("lastName='" + lastName + "'")
                .add("sex='" + sex + "'")
                .add("birthDateTime=" + birthDateTime)
                .add("birthOrder=" + birthOrder)
                .toString();
    }
}
